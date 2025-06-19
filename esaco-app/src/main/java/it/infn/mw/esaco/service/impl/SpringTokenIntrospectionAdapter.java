package it.infn.mw.esaco.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@Service
public class SpringTokenIntrospectionAdapter implements TokenIntrospectionService {

  public static final Logger LOGGER =
      LoggerFactory.getLogger(SpringTokenIntrospectionAdapter.class);

  @Autowired
  private OpaqueTokenIntrospector introspector;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Optional<String> introspectToken(String accessToken) {
    try {
      var auth = introspector.introspect(accessToken);
      String json = objectMapper.writeValueAsString(auth.getAttributes());
      return Optional.of(json);
    } catch (OAuth2IntrospectionException e) {
      LOGGER.warn("Invalid token during introspection: {}", e.getMessage());
      return Optional.empty();
    } catch (UnsupportedIssuerException e) {
      throw new UnsupportedIssuerException(e.getMessage());
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialize token attributes: {}", e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> getUserInfoForToken(String accessToken) {
    // opzionale
    return Optional.empty();
  }
}
