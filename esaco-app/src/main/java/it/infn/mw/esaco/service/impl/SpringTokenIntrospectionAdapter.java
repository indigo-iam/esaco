package it.infn.mw.esaco.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    } catch (Exception e) {
      LOGGER.error("Unexpected error during token introspection", e);
      throw new RuntimeException("Token introspection failed", e);
    }
  }

  @Override
  public Optional<String> getUserInfoForToken(String accessToken) {
    // opzionale
    return Optional.empty();
  }
}
