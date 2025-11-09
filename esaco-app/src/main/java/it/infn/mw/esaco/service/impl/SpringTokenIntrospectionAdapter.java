package it.infn.mw.esaco.service.impl;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.InvalidClientCredentialsException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@Service
public class SpringTokenIntrospectionAdapter implements TokenIntrospectionService {

  public static final Logger LOGGER =
      LoggerFactory.getLogger(SpringTokenIntrospectionAdapter.class);

  private OpaqueTokenIntrospector introspector;
  private ObjectMapper objectMapper;
  private RestTemplate restTemplate;

  public SpringTokenIntrospectionAdapter(OpaqueTokenIntrospector introspector,
      RestTemplate restTemplate, ObjectMapper objectMapper) {

    this.introspector = introspector;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<String> introspectToken(String accessToken) {
    try {
      var auth = introspector.introspect(accessToken);
      String json = objectMapper.writeValueAsString(auth.getAttributes());
      return Optional.of(json);
    } catch (OAuth2IntrospectionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof HttpClientErrorException.Unauthorized http401) {
        LOGGER.warn("Unauthorized client credentials during introspection: {}",
            http401.getResponseBodyAsString());
        throw new InvalidClientCredentialsException("Bad credentials");
      }
      LOGGER.warn("Invalid token during introspection: {}", e.getMessage());
      return Optional.empty();
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialize token attributes: {}", e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> getUserInfoForToken(String accessToken) {
    try {
      String issuer = getIssuerFromAccessToken(accessToken);

      String wellKnownUrl = issuer + ".well-known/openid-configuration";

      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(wellKnownUrl,
          HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});

      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.warn("Unable to retrieve OpenID configuration from well-known endpoint");
        return Optional.empty();
      }

      String userinfoEndpoint = (String) response.getBody().get("userinfo_endpoint");

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<String> userinfoResponse =
          restTemplate.exchange(userinfoEndpoint, HttpMethod.GET, entity, String.class);

      if (userinfoResponse.getStatusCode().is2xxSuccessful()) {
        return Optional.ofNullable(userinfoResponse.getBody());
      } else {
        LOGGER.warn("Call to userinfo endpoint failed with status {}",
            userinfoResponse.getStatusCode());
      }
    } catch (Exception e) {
      LOGGER.error("Error while retrieving endpoint userinfo", e);
    }
    return Optional.empty();
  }

  private String getIssuerFromAccessToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken).getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      throw new TokenValidationException("Error reading issuer claim from access token", e);
    }
  }
}
