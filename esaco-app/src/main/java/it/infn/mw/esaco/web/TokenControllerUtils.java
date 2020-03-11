package it.infn.mw.esaco.web;

import java.text.ParseException;

import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.TokenValidationException;

@RestController
public class TokenControllerUtils {

  protected final String INACTIVE_TOKEN_RESPONSE = "{\"active\":false}";

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