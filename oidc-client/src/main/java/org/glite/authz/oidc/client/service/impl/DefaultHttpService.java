package org.glite.authz.oidc.client.service.impl;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.service.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;

@Service
public class DefaultHttpService implements HttpService {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpService.class);

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public String postWithBasicAuthentication(String endpoint, String username, String password,
      MultiValueMap<String, String> bodyParams) {

    String plainCreds = String.format("%s:%s", username, password);
    String base64Creds = new String(Base64.encode(plainCreds.getBytes()));

    HttpEntity<?> request = buildRequest("Basic " + base64Creds, bodyParams);

    return postHttpRequest(request, endpoint);
  }

  @Override
  public String postWithOAuthAuthentication(String endpoint, String accessToken,
      MultiValueMap<String, String> bodyParams) {

    HttpEntity<?> request = buildRequest("Bearer " + accessToken, new LinkedMultiValueMap<>(0));

    return postHttpRequest(request, endpoint);
  }

  private HttpEntity<?> buildRequest(String authorizationHeaderValue,
      MultiValueMap<String, String> bodyParams) {

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", authorizationHeaderValue);
    return new HttpEntity<>(bodyParams, headers);
  }

  private String postHttpRequest(HttpEntity<?> request, String endpoint) {

    String result = null;
    try {
      result = restTemplate.postForObject(endpoint, request, String.class);
    } catch (HttpClientErrorException httpex) {
      if (Lists.newArrayList(FORBIDDEN.value(), UNAUTHORIZED.value())
        .contains(httpex.getRawStatusCode())) {
        LOGGER.warn("Invalid authorization: '{}' '{}'", endpoint, httpex.getMessage());
      }
    } catch (RestClientException e) {
      LOGGER.error("Error connecting to endpoint: '{}' {}", endpoint, e.getMessage());
      throw new HttpConnectionException(format("Error connecting to endpoint '%s'", endpoint), e);
    }

    return result;
  }

}
