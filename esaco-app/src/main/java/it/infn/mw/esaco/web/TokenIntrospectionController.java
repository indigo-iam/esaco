package it.infn.mw.esaco.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.infn.mw.esaco.service.TokenIntrospectionService;

@RestController
public class TokenIntrospectionController extends TokenControllerUtils {

  @Autowired
  private TokenIntrospectionService tokenIntrospectionService;

  @PostMapping(value = "/introspect",
    produces = MediaType.APPLICATION_JSON_VALUE)
  public String introspectToken(
    @RequestParam(name = "token", required = false) String accessToken) {

    accessTokenSanityChecks(accessToken);

    return tokenIntrospectionService.introspectToken(accessToken)
      .orElse(INACTIVE_TOKEN_RESPONSE);

  }
}