package com.aexp.commercial.data.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class RestUtil {
  private static final Logger logger = LoggerFactory.getLogger(RestUtil.class);

  public static <S, T> ResponseEntity<T> callPostAPI(RestTemplate restTemplate, String url, S request, Class<T> respClass,
                                                     HttpHeaders httpHeaders) {
    logger.info("POST call to API '{}' with request params '{}'", url, request);
    try {
      URI uri = new URI(url);
      HttpEntity<S> httpEntity = new HttpEntity<>(request, httpHeaders);
      ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, respClass);
      logger.info("POST API Response: {}", response);
      return response;
    } catch (Exception e) {
      logger.error("Exception occurred due to: {}", e.getMessage());
      return null;
    }
  }
}
