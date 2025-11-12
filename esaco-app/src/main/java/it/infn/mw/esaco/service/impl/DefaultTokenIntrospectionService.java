package it.infn.mw.esaco.service.impl;

import java.text.ParseException;
import java.util.Objects;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.model.IntrospectionResponse;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@Service
public class DefaultTokenIntrospectionService implements TokenIntrospectionService {

  private final OpaqueTokenIntrospector introspector;

  public DefaultTokenIntrospectionService(OpaqueTokenIntrospector introspector) {

    this.introspector = introspector;
  }

  @Override
  public IntrospectionResponse introspect(String accessToken) throws TokenIntrospectionException {

    try {

      accessTokenSanityChecks(accessToken);

      return new IntrospectionResponse(introspector.introspect(accessToken));

    } catch (Exception e) {

      throw new TokenIntrospectionException(e.getMessage(), e);
    }
  }

  private void accessTokenSanityChecks(String accessToken) {

    if (Objects.isNull(accessToken) || accessToken.isEmpty()) {
      throw new TokenValidationException(
        "Cannot perform request with empty token");
    }

    try {
      JWTParser.parse(accessToken);
    } catch (ParseException e) {
      throw new TokenValidationException("Malformed JWT token string", e);
    }
  }

}
