package com.axp.microdose.commercial.demographic.data.sor.verticles

import com.axp.logging.schema.v0_1.Structured
import com.axp.microdose.commercial.demographic.data.sor.constants.FunctionConstants
import com.axp.microdose.commercial.demographic.data.sor.constants.FunctionConstants.CODE_400
import com.axp.microdose.commercial.demographic.data.sor.constants.FunctionConstants.CODE_500
import com.axp.microdose.commercial.demographic.data.sor.dao.DemographicDataDao
import com.axp.microdose.commercial.demographic.data.sor.dto.ErrorResponse
import com.axp.microdose.commercial.demographic.data.sor.dto.ReadCommercialDemographicDataSorResponseDto
import com.axp.microdose.commercial.demographic.data.sor.dto.UpdateCommercialDemographicDataSorRequestDto
import com.axp.microdose.commercial.demographic.data.sor.dto.ReadEmailDataSorResponseDto
import com.axp.microdose.commercial.demographic.data.sor.enums.ErrorEnum
import com.axp.microdose.commercial.demographic.data.sor.extension.ObjectMapperWrapper.toJson
import com.axp.microdose.commercial.demographic.data.sor.model.CommercialDemographicDataSorResponse
import com.axp.microdose.commercial.demographic.data.sor.model.UpdateCommercialDemographicDataSorRequest
import com.axp.microdose.commercial.demographic.data.sor.utils.FunctionUtils.handleSuccess
import com.axp.microdose.commercial.demographic.data.sor.validators.RequestValidator
import com.axp.microdose.commons.MicrodoseConstants.TRACE_LOG_MESSAGE
import com.axp.microdose.commons.tracing.TracerUtil
import io.vertx.core.MultiMap
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import java.util.*

class UpdateCommercialDemographicDataSorVerticle : BaseVerticle() {

    private lateinit var demographicDataDao: DemographicDataDao

    override suspend fun initFunction(classMarker: Structured) {
        logger.info(classMarker) { "${getFunctionAddress()} started initialization" }
        super.initFunction(classMarker)
        try {
            demographicDataDao = initDemographicDataDao()
        } catch (e: Exception) {
            logger.error(classMarker, "Exception occurred during function initialization", e)
            throw e
        }
        logger.info(classMarker) { "${getFunctionAddress()} finished initialization" }
    }

    override suspend fun handle(message: Message<JsonObject>, messageMarker: Structured) {
        val eventSpan = TracerUtil.createAndStartSpan(getFunctionAddress(), message.headers())
        runCatching {
            val requestBody = message.body()
            val requestHeaders: MultiMap = message.headers()
            logger.info(messageMarker, "Received request: {}, {}", requestBody, requestHeaders)
            val (requestData, errors) =
                RequestValidator.validateUpdateDataRequest(requestBody, requestHeaders, messageMarker)
            if (requestData == null || errors.isNotEmpty()) {
                message.sendReply(HttpStatus.SC_BAD_REQUEST, ErrorResponse(CODE_400, "$errors"))
                eventSpan.finish()
                return
            }
            var demographicData: List<ReadCommercialDemographicDataSorResponseDto>?
            var response: CommercialDemographicDataSorResponse? = null
            val sourceName: String = requestHeaders.get(FunctionConstants.CE_SOURCE)

            /* This is only for E2E testing purpose, just to avoid manual Updates in E2 database. Also, not adding to the Swagger to avoid exposing this to consumers */
            if (requestData.entity != null && requestData.createTimestamp != null) {
                response = demographicDataDao
                    .updateDemographicCreateTime(
                        UpdateCommercialDemographicDataSorRequestDto(
                            null,
                            requestData.entity,
                            null,
                            sourceName,
                            null,
                            requestData.createTimestamp
                        ),
                        messageMarker,
                        requestHeaders
                    )
                eventSpan.finish()
                handleSuccess(
                    message = message,
                    replyMessage = response!!.toJson(),
                    httpStatusCode = HttpStatus.SC_OK.toString()
                )
                return
            }

            if (requestData.requestId != null) {
                demographicData = demographicDataDao.readDemographicData(
                    referenceNumber = null,
                    applicationId = null,
                    companyName = null,
                    accountToken = null,
                    requestData.requestId,
                    messageMarker,
                    message.headers()
                )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read Demographic DB Call by RequestId is finished")
                if (demographicData.isNotEmpty() && (requestData.referenceNumber != null || requestData.entity != null)) {
                    val requestDto = buildDemographicDataDto(requestData, demographicData[0], sourceName)
                    response = demographicDataDao
                        .updateDemographicDataByRequestId(
                            requestDto,
                            messageMarker,
                            requestHeaders
                        )
                    eventSpan?.setTag(TRACE_LOG_MESSAGE, "Update Demographic data by RequestId is finished")
                    logger.info(messageMarker, "Update Demographic data by RequestId:{} is successful.", requestData.requestId)
                }

                val emailResponse = demographicDataDao.readEmailData(
                    requestData.requestId!!,
                    messageMarker,
                    requestHeaders
                )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read Email DB Call is finished")
                if (emailResponse == null) {
                    message.sendReply(
                        HttpStatus.SC_BAD_REQUEST,
                        ErrorResponse(CODE_400, ErrorEnum.INVALID_EMAIL_REQ_ID.errorMessage)
                    )
                    eventSpan.finish()
                    return
                }
                val requestDto = buildEmailDataDto(requestData, emailResponse, sourceName)

                /* Update Reference Number in both email_request and demographic_data tables (if found)
                * Scenario: When channels trigger */
                response = demographicDataDao
                    .updateEmailRequestDataByRequestId(
                        requestDto,
                        messageMarker,
                        requestHeaders
                    )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Update Email data by RequestId is finished")
                logger.info(messageMarker, "Update Email data by RequestId:{} is successful.", requestData.requestId)
            }

            if (requestData.applicationId != null && requestData.requestId == null) {
                demographicData = demographicDataDao.readDemographicData(
                    referenceNumber = null,
                    requestData.applicationId,
                    companyName = null,
                    accountToken = null,
                    requestId = null,
                    messageMarker,
                    message.headers()
                )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read Demographic DB Call by application Id is finished")
                if (demographicData.isEmpty()) {
                    message.sendReply(
                        HttpStatus.SC_BAD_REQUEST,
                        ErrorResponse(CODE_400, ErrorEnum.INVALID_APPLICATION_ID.errorMessage)
                    )
                    eventSpan.finish()
                    return
                }
                val requestDto = buildDemographicDataDto(requestData, demographicData[0], sourceName)
                response = demographicDataDao
                    .updateDemographicDataByApplicationId(
                        requestDto,
                        messageMarker,
                        requestHeaders
                    )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Update Demographic data by ApplicationId is finished")
                logger.info(messageMarker, "Update Demographic data by ApplicationId:{} is successful.", requestData.applicationId)
            }

            if (requestData.referenceNumber != null && requestData.applicationId == null && requestData.requestId == null) {
                demographicData = demographicDataDao.readDemographicData(
                    requestData.referenceNumber,
                    applicationId = null,
                    companyName = null,
                    accountToken = null,
                    requestId = null,
                    messageMarker,
                    message.headers()
                )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Read Demographic DB Call by Reference number is finished")
                if (demographicData.isEmpty()) {
                    message.sendReply(
                        HttpStatus.SC_BAD_REQUEST,
                        ErrorResponse(CODE_400, ErrorEnum.INVALID_REF_NUMBER.errorMessage)
                    )
                    eventSpan.finish()
                    return
                }
                val requestDto = buildDemographicDataDto(requestData, demographicData[0], sourceName)
                response = demographicDataDao
                    .updateDemographicDataByRefNumber(
                        requestDto,
                        messageMarker,
                        requestHeaders
                    )
                eventSpan?.setTag(TRACE_LOG_MESSAGE, "Update Demographic data by Reference Number is finished")
                logger.info(messageMarker, "Update Demographic data by Reference Number:{} is successful.", requestData.referenceNumber)
            }

            eventSpan.finish()
            handleSuccess(
                message = message,
                replyMessage = response!!.toJson(),
                httpStatusCode = HttpStatus.SC_OK.toString()
            )
        }.onFailure { e: Throwable ->
            TracerUtil.finishSpanWithFailure(eventSpan, e)
            logger.error(messageMarker, "Exception occurred during handling: {}", e.stackTraceToString())
            message.sendReply(HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorResponse(CODE_500, e.message))
        }
    }

    private fun buildDemographicDataDto(
        sorRequest: UpdateCommercialDemographicDataSorRequest,
        demographicSorResponseData: ReadCommercialDemographicDataSorResponseDto,
        sourceName: String,
    ): UpdateCommercialDemographicDataSorRequestDto {
        return UpdateCommercialDemographicDataSorRequestDto(
            referenceNumber = sorRequest.referenceNumber ?: demographicSorResponseData.referenceNumber,
            entity = sorRequest.entity ?: demographicSorResponseData.entity,
            applicationId = sorRequest.applicationId,
            sourceName = sourceName,
            companyName = sorRequest.companyName ?: demographicSorResponseData.companyName,
            createTimestamp = null,
            requestId = sorRequest.requestId
        )
    }

    private fun buildEmailDataDto(
        sorRequest: UpdateCommercialDemographicDataSorRequest,
        emailResponse: ReadEmailDataSorResponseDto,
        sourceName: String,
    ): UpdateCommercialDemographicDataSorRequestDto {
        var applicationId: UUID? = null
        if(emailResponse.applicationId != null && sorRequest.applicationId == null) {
            applicationId = emailResponse.applicationId
        } else if(sorRequest.applicationId != null) {
            applicationId = sorRequest.applicationId
        }
        return UpdateCommercialDemographicDataSorRequestDto(
            referenceNumber = sorRequest.referenceNumber ?: emailResponse.referenceNumber,
            entity = sorRequest.entity ?: emailResponse.entity,
            applicationId = applicationId,
            sourceName = sourceName,
            companyName = null,
            createTimestamp = null,
            requestId = emailResponse.requestId
        )
    }
}
