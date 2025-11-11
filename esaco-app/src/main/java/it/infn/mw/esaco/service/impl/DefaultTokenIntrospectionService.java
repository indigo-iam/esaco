package it.infn.mw.esaco.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.InvalidClientCredentialsException;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@Service
public class DefaultTokenIntrospectionService implements TokenIntrospectionService {

  public static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultTokenIntrospectionService.class);

  private final OpaqueTokenIntrospector introspector;
  private final ObjectMapper objectMapper;

  public DefaultTokenIntrospectionService(OpaqueTokenIntrospector introspector,
      ObjectMapper objectMapper) {

    this.introspector = introspector;
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<String> introspect(String accessToken) throws DiscoveryDocumentNotFoundException {

    OAuth2AuthenticatedPrincipal auth = null;
    try {

      auth = introspector.introspect(accessToken);

    } catch (OAuth2IntrospectionException e) {

      Throwable cause = e.getCause();
      if (cause instanceof HttpClientErrorException.Unauthorized http401) {
        LOGGER.warn("Unauthorized client credentials during introspection: {}",
            http401.getResponseBodyAsString());
        throw new InvalidClientCredentialsException("Bad credentials");
      }
      LOGGER.warn("Invalid token during introspection: {}", e.getMessage());
      return Optional.empty();
    }

    try {

      return Optional.of(objectMapper.writeValueAsString(auth.getAttributes()));

    } catch (JsonProcessingException e) {

      LOGGER.error("Failed to serialize token attributes: {}", e.getMessage());
      return Optional.empty();
    }
  }


}
