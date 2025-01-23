package com.axp.microdose.commercial.demographic.data.verticles

import com.axp.logging.schema.v0_1.Structured
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.A2A_CONFIG
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.CM15
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.CODE_400
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.NAICS_CODE_TYPE
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.READ_C360
import com.axp.microdose.commercial.demographic.data.enums.ErrorEnum
import com.axp.microdose.commercial.demographic.data.enums.NonSensitiveDataFieldEnum
import com.axp.microdose.commercial.demographic.data.model.*
import com.axp.microdose.commercial.demographic.data.model.c360.read.ReadCustomerIdentityAndRelationshipResponse
import com.axp.microdose.commercial.demographic.data.utils.C360FunctionUtil.buildReadC360Request
import com.axp.microdose.commercial.demographic.data.validators.RequestValidator
import com.axp.microdose.commons.MicrodoseConstants
import com.axp.microdose.commons.MicrodoseConstants.TRACE_LOG_MESSAGE
import com.axp.microdose.commons.tracing.TracerUtil
import io.opentracing.Span
import io.vertx.core.MultiMap
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

class ReadCommercialDemographicDataVerticle : BaseVerticle() {

    override suspend fun handle(message: Message<JsonObject>, messageMarker: Structured) {
        val eventSpan = TracerUtil.createAndStartSpan(getFunctionAddress(), message.headers())
        runCatching {
            val missingNonSensitiveFields: ArrayList<String>?
            val requestBody = message.body()
            val requestHeaders: MultiMap = message.headers()
            val authConfig = getLambdaConfig().getJsonObject(A2A_CONFIG)
            var readCommercialDemographicDataResponse: ReadCommercialDemographicDataResponse? = null
            logger.info(messageMarker, "Received request: {}, {}", requestBody, requestHeaders)
            val (request, errors) =
                RequestValidator.validateReadDataRequest(requestBody, requestHeaders, messageMarker)
            if (request == null || errors.isNotEmpty()) {
                message.sendReply(HttpStatus.SC_BAD_REQUEST, ErrorResponse(CODE_400, "$errors"))
                eventSpan.finish()
                return
            }

            /* TODO: Remove this if block once all the consumers migrated to use account object */
            if (request.account == null && request.accountToken != null) {
                request.account = Account(request.accountToken!!, CM15)
            }

            val scopes = ArrayList<String>()
            Optional.ofNullable<JsonArray>(authConfig.getJsonArray(FunctionConstants.SCOPES)).orElse(JsonArray())
                .stream()
                .forEach { scope: Any -> scopes.add(scope as String) }

            val readSorResponse = callOneDataFunction(
                getDependentAddress(FunctionConstants.READ_SOR_FILE),
                createA2AHeaders(message.headers(), messageMarker, scopes),
                JsonObject(gson.toJson(request)),
                messageMarker,
            )
            eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read SOR Call is finished")

            if (readSorResponse.body != null && readSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 200) {
                val sorResponseBody = readSorResponse.body
                log.info(messageMarker, "Read Demographic Data SOR successful.")
                val readDataSorResponse = gson.fromJson(
                    sorResponseBody.toString(),
                    ReadCommercialDemographicDataSorResponses::class.java
                )
                if (readDataSorResponse.applications.isNotEmpty()) {
                    readCommercialDemographicDataResponse = buildReadSorResponse(readDataSorResponse)
                }
                if (readCommercialDemographicDataResponse?.lastApplication?.account != null) {
                    missingNonSensitiveFields = fetchMissingNSDFieldsFromC360(
                        readCommercialDemographicDataResponse,
                        message,
                        messageMarker,
                        scopes,
                        eventSpan
                    )
                    if (!missingNonSensitiveFields.isNullOrEmpty()) {
                        readCommercialDemographicDataResponse.isNonSensitiveDataRequired = true
                        readCommercialDemographicDataResponse.isDataCapturedInLast36Months = false
                        readCommercialDemographicDataResponse.lastApplication?.missingNonSensitiveFields =
                            missingNonSensitiveFields
                    }
                }
                message.sendReply(
                    HttpStatus.SC_OK,
                    readCommercialDemographicDataResponse!!
                )
                eventSpan.finish()
                return
            } else if (readSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 204) {
                message.sendReply(
                    HttpStatus.SC_OK,
                    ReadCommercialDemographicDataResponse(
                        isDataCapturedInLast36Months = false,
                        isSensitiveDataRequired = true,
                        isNonSensitiveDataRequired = true
                    )
                )
                eventSpan.finish()
                return
            } else {
                val sorErrorResponseBody = readSorResponse.body
                val readDataSorErrorResponse = gson.fromJson(
                    sorErrorResponseBody.toString(),
                    ErrorResponse::class.java
                )
                log.info(messageMarker, "Read Demographic Data SOR failed. {}", readDataSorErrorResponse)
                message.sendReply(
                    readSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt(),
                    readDataSorErrorResponse
                )
                eventSpan.finish()
                return
            }
        }.onFailure { e: Throwable ->
            TracerUtil.finishSpanWithFailure(eventSpan, e)
            logger.error(messageMarker, "Request failed with an exception ", e)
            respondException(message, messageMarker, e)
        }
    }

    private suspend fun fetchMissingNSDFieldsFromC360(
        readCommercialDemographicDataResponse: ReadCommercialDemographicDataResponse?,
        message: Message<JsonObject>, messageMarker: Structured, scopes: ArrayList<String>,
        eventSpan: Span,
    ): ArrayList<String>? {
        var missingNonSensitiveFields: ArrayList<String>? = null
        val accountId = readCommercialDemographicDataResponse?.lastApplication?.account?.id
        val accountIdType: String? = readCommercialDemographicDataResponse?.lastApplication?.account?.type
        val c360Response = callOneDataFunction(
            getDependentAddress(READ_C360),
            createA2AHeaders(message.headers(), messageMarker, scopes),
            JsonObject(gson.toJson(buildReadC360Request(accountId!!, accountIdType!!))),
            messageMarker
        )
        eventSpan.setTag(TRACE_LOG_MESSAGE, "Read C360 Call is finished")
        eventSpan.finish()
        // if there's no response from C360, exit out early
        if (c360Response.body == null) {
            log.error(
                messageMarker,
                "Read profile from C360 failed. No relationships found. AccountId: {}",
                accountId
            )
            message.sendReply(
                HttpStatus.SC_BAD_REQUEST,
                ErrorResponse(CODE_400, ErrorEnum.INVALID_ACCOUNT_ID.errorMessage)
            )
        } else {
            log.info(messageMarker, "Read from C360 successful.")
            val readC360Response = (c360Response.body as JsonObject).mapTo(
                ReadCustomerIdentityAndRelationshipResponse::class.java
            )
            if (readC360Response.relationshipsForAccount != null && readC360Response.relationshipsForAccount!!.isNotEmpty()) {
                missingNonSensitiveFields = ArrayList()
                for (relationship in readC360Response.relationshipsForAccount!!) {
                    if (relationship.contactProfile != null && (relationship.contactProfile!!.postalAddresses == null
                                || relationship.contactProfile!!.postalAddresses!!.business == null)
                    ) {
                        missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.ADDRESS.name)
                    }
                    if (relationship.identityProfile != null && relationship.identityProfile!!.businessInformation != null) {
                        if (relationship.identityProfile!!.businessInformation!!.numberOfBusinessEmployees == null ||
                            relationship.identityProfile!!.businessInformation!!.numberOfBusinessEmployees!!.numberOfEmployees == null
                        ) {
                            missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NUMBER_OF_WORKERS.name)
                        }

                        if (relationship.identityProfile!!.businessInformation!!.standardIndustryCode == null) {
                            missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NAICS_CODE.name)
                        } else if (relationship.identityProfile!!.businessInformation!!.standardIndustryCode != null &&
                            relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther.isNullOrEmpty()
                        ) {
                            missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NAICS_CODE.name)
                        } else if (relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther != null &&
                            relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther!!.isNotEmpty() &&
                            relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther!!.singleOrNull {
                                it.type == NAICS_CODE_TYPE
                            } == null
                        ) {
                            missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NAICS_CODE.name)
                        }
                        if (relationship.identityProfile!!.businessInformation!!.timeInBusiness == null) {
                            missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.TIME_IN_BUSINESS.name)
                        }
                    } else {
                        missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NUMBER_OF_WORKERS.name)
                        missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.NAICS_CODE.name)
                        missingNonSensitiveFields.add(NonSensitiveDataFieldEnum.TIME_IN_BUSINESS.name)
                    }
                }
            }
        }
        return missingNonSensitiveFields
    }

    private fun buildReadSorResponse(
        sorResponses: ReadCommercialDemographicDataSorResponses,
    ): ReadCommercialDemographicDataResponse {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val collectedDate = LocalDate.parse(sorResponses.applications[0].collectionDate, dateFormatter)
        val currentDate = LocalDate.parse(LocalDate.now().toString(), dateFormatter)
        val application = sorResponses.applications[0]
        var isSensitiveDataRequired = false
        if (application.businessOwnershipStatus == null && application.principalOwners == null) {
            isSensitiveDataRequired = true
        }
        if (Period.between(collectedDate, currentDate).toTotalMonths() > 36) {
            isSensitiveDataRequired = true
        }

        val lastApplication = LastApplication(
            application.applicationId,
            application.referenceNumber,
            application.requestId,
            application.entity,
            application.companyName,
            application.collectionDate,
            application.account,
        )
        val responses = ReadCommercialDemographicDataResponse(
            !isSensitiveDataRequired,
            isSensitiveDataRequired,
            false,
            lastApplication
        )
        return responses
    }

}
