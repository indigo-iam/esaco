package it.infn.mw.esaco.web;

import java.text.ParseException;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.TokenValidationException;

public class TokenControllerUtils {

  protected static final String INACTIVE_TOKEN_RESPONSE = "{\"active\":false}";

  protected void accessTokenSanityChecks(String accessToken) {

    if (Strings.isNullOrEmpty(accessToken)) {
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
