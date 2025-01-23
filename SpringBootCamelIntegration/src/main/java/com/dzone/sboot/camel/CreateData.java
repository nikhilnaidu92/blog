package com.axp.microdose.commercial.demographic.data.verticles

import com.axp.logging.schema.v0_1.Structured
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.A2A_CONFIG
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.CM15
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.CODE_400
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.CREATE_SOR_FILE
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.READ_C360
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.READ_EMAIL_SOR_FILE
import com.axp.microdose.commercial.demographic.data.constants.FunctionConstants.UPDATE_C360
import com.axp.microdose.commercial.demographic.data.enums.AddressTypeEnum
import com.axp.microdose.commercial.demographic.data.enums.EntityType
import com.axp.microdose.commercial.demographic.data.enums.ErrorEnum
import com.axp.microdose.commercial.demographic.data.model.*
import com.axp.microdose.commercial.demographic.data.model.c360.read.ReadCustomerIdentityAndRelationshipResponse
import com.axp.microdose.commercial.demographic.data.utils.C360FunctionUtil.buildC360UpdateRequest
import com.axp.microdose.commercial.demographic.data.utils.C360FunctionUtil.buildReadC360Request
import com.axp.microdose.commercial.demographic.data.utils.C360FunctionUtil.fetchEntityIdAndCategoryCode
import com.axp.microdose.commercial.demographic.data.validators.RequestValidator
import com.axp.microdose.commons.MicrodoseConstants
import com.axp.microdose.commons.MicrodoseConstants.TRACE_LOG_MESSAGE
import com.axp.microdose.commons.tracing.TracerUtil
import io.vertx.core.MultiMap
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import java.util.*

class CreateCommercialDemographicDataVerticle : BaseVerticle() {

    override suspend fun handle(message: Message<JsonObject>, messageMarker: Structured) {
        val eventSpan = TracerUtil.createAndStartSpan(getFunctionAddress(), message.headers())
        runCatching {
            val requestBody = message.body()
            val requestHeaders: MultiMap = message.headers()
            val authConfig = getLambdaConfig().getJsonObject(A2A_CONFIG)
            var shouldUpdateC360 = false
            var categoryCode: String? = null
            log.info(messageMarker, "Received request: {}, {}", requestBody, requestHeaders)
            val (request, errors) =
                RequestValidator.validateCreateDataRequest(requestBody, requestHeaders, messageMarker)
            if (request == null || errors.isNotEmpty()) {
                message.sendReply(HttpStatus.SC_BAD_REQUEST, ErrorResponse(CODE_400, "$errors"))
                eventSpan.finish()
                return
            }
            val scopes = ArrayList<String>()
            Optional.ofNullable<JsonArray>(authConfig.getJsonArray(FunctionConstants.SCOPES)).orElse(JsonArray())
                .stream()
                .forEach { scope: Any -> scopes.add(scope as String) }

            /* TODO: Remove this if block once all the consumers migrated to use account object */
            if (request.account == null && request.accountToken != null) {
                request.account = Account(request.accountToken!!, CM15)
            }

            /* Check if the reference number (PCN) OR email request id OR application id exists in DFO */
            val readSorResponse = callOneDataFunction(
                getDependentAddress(FunctionConstants.READ_SOR_FILE),
                createA2AHeaders(message.headers(), messageMarker, scopes),
                JsonObject(
                    gson.toJson(
                        buildReadDataSorRequest(request)
                    )
                ),
                messageMarker,
            )
            eventSpan.setTag(TRACE_LOG_MESSAGE, "Read SOR Call is finished")
            if (readSorResponse.body != null && readSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 200) {
                log.info(
                    messageMarker,
                    "Dodd Frank data is successfully returned by the SOR for the given Application Id/Account Token"
                )
                val readDataSorResponse: ReadCommercialDemographicDataSorResponse = gson.fromJson(
                    readSorResponse.body.toString(),
                    ReadCommercialDemographicDataSorResponses::class.java
                ).applications[0]
                val demographicDataResponse = CommercialDemographicDataResponse(
                    readDataSorResponse.referenceNumber,
                    readDataSorResponse.applicationId,
                    readDataSorResponse.entity,
                    readDataSorResponse.requestId,
                    readDataSorResponse.companyName,
                    readDataSorResponse.collectionDate,
                    readDataSorResponse.account
                )
                if ((request.useLastApplicationData != null && request.useLastApplicationData == true) || request.applicationId != null) {
                    log.info(
                        messageMarker,
                        "Copying the existing DF data to the new reference number:{}", request.referenceNumber
                    )
                    request.account = readDataSorResponse.account
                    request.businessOwnershipStatus = readDataSorResponse.businessOwnershipStatus
                    request.numberOfPrincipalOwners = readDataSorResponse.numberOfPrincipalOwners
                    request.principalOwners = readDataSorResponse.principalOwners
                    request.entity = readDataSorResponse.entity
                    request.companyName = readDataSorResponse.companyName
                } else {
                    log.warn(
                        messageMarker,
                        "Dodd Frank data already exists for the given Email Request ID/Reference Number"
                    )
                    message.sendReply(HttpStatus.SC_OK, demographicDataResponse)
                    eventSpan.finish()
                    return
                }
            } else if (readSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 204) {
                log.info(messageMarker, "Read Demographic Data SOR returned empty response.")
            } else {
                val readDataSorErrorResponse = gson.fromJson(
                    readSorResponse.body.toString(),
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

            /* Call Read Email function to fetch Entity, Reference Number and Account Token for an existing Email Request ID */
            if (request.requestId != null) {
                val emailSorResponse = callOneDataFunction(
                    getDependentAddress(READ_EMAIL_SOR_FILE),
                    createA2AHeaders(message.headers(), messageMarker, scopes),
                    JsonObject(gson.toJson(ReadCommercialDemographicEmailSorRequest(request.requestId))),
                    messageMarker
                )
                eventSpan.setTag(TRACE_LOG_MESSAGE, "Read Email SOR Call is finished")
                if (emailSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 200 && emailSorResponse.body != null) {
                    val emailResponse = gson.fromJson(
                        emailSorResponse.body.toString(),
                        EmailResponses::class.java
                    ).requests[0]
                    request.account = emailResponse.account
                    if (emailResponse.companyName != null) {
                        request.companyName = emailResponse.companyName
                    }
                    request.entity = emailResponse.entity
                    request.referenceNumber = emailResponse.referenceNumber
                } else {
                    message.sendReply(
                        HttpStatus.SC_BAD_REQUEST,
                        ErrorResponse(CODE_400, ErrorEnum.INVALID_REQUEST_ID.errorMessage)
                    )
                    eventSpan.finish()
                    return
                }
            }

            /* Check if NSD fields are passed, and set this flag to true. For all the requests with account token, we call C360 and fetch NSD from C360 and set to request payload */
            if (isNsdPassed(request)) {
                shouldUpdateC360 = true
            }

            /* Call C360 using Account Token to fetch Entity (in case if upstream sends a token) and Category code (to update NSD fields) */
            if (request.account != null) {
                val c360Response = callOneDataFunction(
                    getDependentAddress(READ_C360),
                    createA2AHeaders(message.headers(), messageMarker, scopes),
                    JsonObject(gson.toJson(buildReadC360Request(request.account!!.id, request.account!!.type))),
                    messageMarker
                )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read C360 Call is finished")

                // if there's no response from C360, exit out early
                if (c360Response.body == null) {
                    log.error(
                        messageMarker,
                        "Read profile from C360 failed. No relationships found for Account Id: {}",
                        request.account!!.id
                    )
                    message.sendReply(
                        HttpStatus.SC_BAD_REQUEST,
                        ErrorResponse(CODE_400, ErrorEnum.INVALID_ACCOUNT_ID.errorMessage)
                    )
                    eventSpan.finish()
                    return
                } else {
                    log.info(messageMarker, "Read from C360 successful.")
                    val readC360Response = (c360Response.body as JsonObject).mapTo(
                        ReadCustomerIdentityAndRelationshipResponse::class.java
                    )

                    if (readC360Response.relationshipsForAccount != null && readC360Response.relationshipsForAccount!!.isNotEmpty()) {
                        for (relationship in readC360Response.relationshipsForAccount!!) {
                            if (relationship.contactProfile != null && (relationship.contactProfile!!.postalAddresses != null
                                        && relationship.contactProfile!!.postalAddresses!!.business != null && request.address == null)
                            ) {
                                val c360Address = relationship.contactProfile!!.postalAddresses!!.business
                                request.address = Address(
                                    c360Address?.line1!!,
                                    c360Address.line2,
                                    c360Address.line3,
                                    c360Address.line4,
                                    AddressTypeEnum.BUSINESS.name,
                                    c360Address.city!!,
                                    c360Address.state!!,
                                    c360Address.postalCode!!,
                                    c360Address.alphaCountryCode!!,
                                )
                            }
                            if (relationship.identityProfile != null && relationship.identityProfile!!.businessInformation != null) {
                                if (relationship.identityProfile!!.businessInformation!!.numberOfBusinessEmployees != null &&
                                    relationship.identityProfile!!.businessInformation!!.numberOfBusinessEmployees!!.numberOfEmployees != null &&
                                    request.employeeCount == null
                                ) {
                                    request.employeeCount =
                                        relationship.identityProfile!!.businessInformation!!.numberOfBusinessEmployees!!.numberOfEmployees
                                }
                                if (relationship.identityProfile!!.businessInformation!!.standardIndustryCode != null &&
                                    relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther != null &&
                                    relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther!!.isNotEmpty() &&
                                    relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther!!.singleOrNull {
                                        it.type == FunctionConstants.NAICS_CODE_TYPE
                                    } != null && request.naicsCode == null
                                ) {
                                    request.naicsCode =
                                        relationship.identityProfile!!.businessInformation!!.standardIndustryCode!!.standardIndustryCodeOther!!.singleOrNull {
                                            it.type == FunctionConstants.NAICS_CODE_TYPE
                                        }?.code
                                }
                                if (relationship.identityProfile!!.businessInformation!!.timeInBusiness != null && request.timeInBusiness == null) {
                                    request.timeInBusiness =
                                        relationship.identityProfile!!.businessInformation!!.timeInBusiness?.numberOfYear
                                }
                            }

                            relationship.identityProfile?.businessInformation?.let { businessInformation ->
                                businessInformation.numberOfBusinessEmployees?.numberOfEmployees?.let {
                                    if (request.employeeCount == null) {
                                        request.employeeCount = it
                                    }
                                }
                                businessInformation.standardIndustryCode?.let { standardIndustryCode ->
                                    standardIndustryCode.standardIndustryCodeOther?.let { standardIndustryCodeOther ->
                                        if (standardIndustryCodeOther.isNotEmpty() && standardIndustryCodeOther.singleOrNull()?.type == FunctionConstants.NAICS_CODE_TYPE
                                            && request.naicsCode == null
                                        ) {
                                            request.naicsCode = standardIndustryCodeOther.singleOrNull()?.code
                                        }
                                    }
                                }
                                businessInformation.timeInBusiness?.let {
                                    if (request.timeInBusiness == null) {
                                        request.timeInBusiness = it.numberOfYear
                                    }
                                }
                            }
                        }
                    }

                    val (entity, code) = fetchEntityIdAndCategoryCode(readC360Response)
                    if (entity != null && code != null) {
                        categoryCode = code
                        request.entity = entity
                    } else {
                        log.error(
                            messageMarker,
                            "Entity Id or Category Code is missing in C360"
                        )
                        message.sendReply(
                            HttpStatus.SC_BAD_REQUEST,
                            ErrorResponse(CODE_400, ErrorEnum.MISSING_ENTITY_ID_IN_C360.errorMessage)
                        )
                        eventSpan.finish()
                        return
                    }
                }
            }

            /* Build Create Demographic SOR request payload */
            val createSorRequest = buildCreateDataSorRequest(request)

            /* Call Create Demographic SOR to store the data in DB */
            val createSorResponse = callOneDataFunction(
                getDependentAddress(CREATE_SOR_FILE),
                createA2AHeaders(message.headers(), messageMarker, scopes),
                createSorRequest.toJson(),
                messageMarker,
            )
            eventSpan?.setTag(TRACE_LOG_MESSAGE, "Create Demographic SOR Call is finished")
            if ((createSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 200 ||
                        createSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt() == 201)
            ) {
                log.info(messageMarker, "Create Demographic Data SOR successful.")
                val createDataSorResponse = gson.fromJson(
                    createSorResponse.body.toString(),
                    CommercialDemographicDataResponse::class.java
                )
                if (isNsdPassed(request) && shouldUpdateC360 && request.account != null && request.entity?.type!! != EntityType.CRO_ID.name
                ) {
                    callOneDataFunctionWithArray(
                        getDependentAddress(UPDATE_C360),
                        createA2AHeaders(message.headers(), messageMarker, scopes),
                        JsonArray(
                            gson.toJson(
                                buildC360UpdateRequest(
                                    categoryCode!!,
                                    request.address,
                                    request.naicsCode,
                                    request.employeeCount,
                                    request.timeInBusiness,
                                    request.account!!.id
                                )
                            )
                        ),
                        messageMarker,
                    )
                    log.info(messageMarker, "Update C360 call is successful.")
                    eventSpan?.setTag(TRACE_LOG_MESSAGE, "Update C360 Call is finished")
                }
                message.sendReply(
                    createSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt(),
                    createDataSorResponse
                )
                eventSpan.finish()
                return
            } else {
                val createDataSorErrorResponse = gson.fromJson(
                    createSorResponse.body.toString(),
                    ErrorResponse::class.java
                )
                log.info(messageMarker, "Create Demographic Data SOR failed. {}", createDataSorErrorResponse)
                message.sendReply(
                    createSorResponse.headers[MicrodoseConstants.HTTP_STATUS_CODE].toInt(),
                    createDataSorErrorResponse
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

    private fun isNsdPassed(request: CreateCommercialDemographicDataRequest): Boolean {
        return (request.address != null || request.naicsCode != null || request.employeeCount != null ||
                request.timeInBusiness != null) && (request.applicationId == null && (request.useLastApplicationData == null || request.useLastApplicationData == false))
    }

    private fun buildReadDataSorRequest(request: CreateCommercialDemographicDataRequest): ReadCommercialDemographicDataSorRequest {
        val readDataSorRequest = if (request.applicationId != null) {
            ReadCommercialDemographicDataSorRequest(request.applicationId, null, null, null)
        } else if ((request.useLastApplicationData != null && request.useLastApplicationData == true) && request.account != null) {
            ReadCommercialDemographicDataSorRequest(
                null,
                null,
                null,
                request.account
            )
        } else if (request.referenceNumber != null) {
            ReadCommercialDemographicDataSorRequest(
                null,
                request.referenceNumber,
                null,
                null
            )
        } else {
            ReadCommercialDemographicDataSorRequest(
                null,
                null,
                request.requestId,
                null
            )
        }
        return readDataSorRequest
    }

    private fun buildCreateDataSorRequest(
        request: CreateCommercialDemographicDataRequest,
    ): CreateCommercialDemographicDataSorRequest {
        var requestId: String? = null
        if (request.requestId != null) {
            requestId = request.requestId!!
        }
        val createSorRequest = CreateCommercialDemographicDataSorRequest(
            request.referenceNumber,
            request.entity,
            request.account,
            requestId,
            request.companyName,
            request.businessOwnershipStatus,
            request.principalOwners,
            request.numberOfPrincipalOwners,
            request.address,
            request.naicsCode,
            request.employeeCount,
            request.timeInBusiness,
        )
        return createSorRequest
    }
}
