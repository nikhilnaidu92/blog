package com.aexp.commercial.data.service;

import com.aexp.commercial.data.config.BatchPropertyConfig;
import com.aexp.commercial.data.model.raven.RavenRequest;
import com.aexp.commercial.data.util.CbisUtil;
import com.aexp.commercial.data.util.RestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.aexp.commercial.data.constants.BatchConstants.AUTHORIZATION;
import static com.aexp.commercial.data.constants.BatchConstants.BEARER;
import static com.aexp.commercial.data.constants.BatchConstants.CE_SOURCE;
import static com.aexp.commercial.data.constants.BatchConstants.CLIENT_ID;
import static com.aexp.commercial.data.constants.BatchConstants.CONTENT_TYPE_JSON;
import static com.aexp.commercial.data.constants.BatchConstants.CORRELATION_ID;
import static com.aexp.commercial.data.constants.BatchConstants.TRACKING_ID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;


/**
 * Raven client to send email.
 */
@Lazy
@Component
public class RavenCommunicationService {
  private static final String CLASS_NAME = RavenCommunicationService.class.getSimpleName();

  private final Logger logger = LogManager.getLogger(CLASS_NAME);

  private final BatchPropertyConfig batchPropertyConfig;

  private final RestTemplate restTemplate;

  @Autowired
  public RavenCommunicationService(BatchPropertyConfig batchPropertyConfig, RestTemplate restTemplate) {
    this.batchPropertyConfig = batchPropertyConfig;
    this.restTemplate = restTemplate;
  }

  public void sendEmail(RavenRequest ravenRequest, UUID emailRequestId, String trackingId) {
    ResponseEntity<String> response;
    try {
      HttpHeaders headersMap = buildHttpHeaders(trackingId,
        CbisUtil.generateBearerToken(restTemplate, batchPropertyConfig.getCbisUrl(), batchPropertyConfig.getClientId(),
          batchPropertyConfig.getClientSecret(), batchPropertyConfig.getClientVersion()));
      logger.info("Processing Raven Request Tracking ID: {} and DFO Email Request Id: {} ", trackingId, emailRequestId);

      response = RestUtil.callPostAPI(restTemplate, batchPropertyConfig.getRavenUrl(), ravenRequest, String.class, headersMap);
      if (response != null) {
        logger.info("Raven Response Body: {} for Tracking Id: {} and DFO Email Request Id: {}", response.getBody(), trackingId,
          emailRequestId);
      }
    } catch (Exception e) {
      logger.error("Failed Raven Tracking Id: {} and DFO Email Request ID :{} with Error :{} ", trackingId, emailRequestId,
        e.getMessage());
    }
  }

  private HttpHeaders buildHttpHeaders(String trackingId, String token) {
    var headers = new HttpHeaders();
    headers.set(CONTENT_TYPE, CONTENT_TYPE_JSON);
    headers.set(CORRELATION_ID, trackingId);
    headers.set(CLIENT_ID, batchPropertyConfig.getClientId());
    headers.set(AUTHORIZATION, BEARER + token);
    headers.set(TRACKING_ID, trackingId);
    headers.set(CE_SOURCE, "CommercialDemographicData");
    return headers;
  }
}
