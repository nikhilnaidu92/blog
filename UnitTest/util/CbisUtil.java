package com.aexp.commercial.data.util;

import com.aexp.commercial.data.model.cbis.CBISResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class CbisUtil {

  private static final Logger logger = LoggerFactory.getLogger(CbisUtil.class);
  private static final String HEADER_ACCEPT = "Accept";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_AUTH_APP_ID = "X-Auth-AppID";
  private static final String HEADER_AUTH_SIGNATURE = "X-Auth-Signature";
  private static final String HEADER_AUTH_TIMESTAMP = "X-Auth-Timestamp";
  private static final String HEADER_AUTH_VERSION = "X-Auth-Version";
  private static final String APPLICATION_JSON = "application/json";

  private CbisUtil() {
    // Private constructor to prevent instantiation
  }

  public static String generateBearerToken(RestTemplate restTemplate, String cbisUrl, String cbisClientId, String cbisClientSecret,
                                           String cbisClientVersion)
    throws Exception {
    try {
      long timestamp = Instant.now().toEpochMilli();
      String input = cbisClientId + "-" + cbisClientVersion + "-" + timestamp;
      String b64Signature = generateSignature(input, cbisClientSecret);
      if (b64Signature == null) {
        logger.error("CBIS call failed due to incorrect Base64 signature.");
        throw new Exception("Error generating the Base64 signature");
      } else {
        b64Signature = b64Signature.substring(0, b64Signature.length() - 1);
      }
      String requestBody = "{\"scope\": [\"*\"]}";
      HttpHeaders httpHeaders = createHttpHeaders(cbisClientId, b64Signature, cbisClientVersion, timestamp);

      ResponseEntity<CBISResponse> response = RestUtil.callPostAPI(restTemplate, cbisUrl, requestBody, CBISResponse.class, httpHeaders);
      if (response != null) {
        return response.getBody() != null ? response.getBody().getAuthorizationToken() : null;
      }
      return null;
    } catch (Exception e) {
      logger.error("Error generating the JWT: {}", e.getMessage(), e);
      throw new Exception("Error generating the JWT");
    }
  }

  private static String generateSignature(String input, String secret) {
    try {
      byte[] secretBytes = Base64.getDecoder().decode(secret);
      byte[] signatureBytes = calculateHmacSHA256(input.getBytes(StandardCharsets.UTF_8), secretBytes);
      return Base64.getEncoder().encodeToString(signatureBytes)
        .replace("+", "-")
        .replace("/", "_");
    } catch (Exception e) {
      logger.error("Error generating signature: {}", e.getMessage(), e);
      return null;
    }
  }

  private static byte[] calculateHmacSHA256(byte[] data, byte[] key) {
    try {
      Mac hmacSha256 = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
      hmacSha256.init(secretKey);
      return hmacSha256.doFinal(data);
    } catch (Exception e) {
      logger.error("Error in calculateHmacSHA256 : {}", e.getMessage());
      return new byte[0];
    }
  }

  private static HttpHeaders createHttpHeaders(String clientId, String signature, String cbisClientVersion, long timestamp) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HEADER_ACCEPT, APPLICATION_JSON);
    headers.set(HEADER_CONTENT_TYPE, APPLICATION_JSON);
    headers.set(HEADER_AUTH_APP_ID, clientId);
    headers.set(HEADER_AUTH_SIGNATURE, signature);
    headers.set(HEADER_AUTH_TIMESTAMP, String.valueOf(timestamp));
    headers.set(HEADER_AUTH_VERSION, cbisClientVersion);
    return headers;
  }

}
