package it.infn.mw.esaco.web;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.InvalidClientCredentialsException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@RestController
public class TokenIntrospectionController extends TokenControllerUtils {

  private TokenIntrospectionService tokenIntrospectionService;

  public TokenIntrospectionController(TokenIntrospectionService tokenIntrospectionService) {

    this.tokenIntrospectionService = tokenIntrospectionService;
  }

  @PostMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE)
  public String introspectToken(
      @RequestParam(value = OAuth2ParameterNames.TOKEN, required = true) String accessToken)
      throws InvalidClientCredentialsException, TokenValidationException, UnsupportedIssuerException, DiscoveryDocumentNotFoundException {

    accessTokenSanityChecks(accessToken);

    return tokenIntrospectionService.introspect(accessToken).orElse(INACTIVE_TOKEN_RESPONSE);
  }
}
