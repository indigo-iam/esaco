package it.infn.mw.esaco.web;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.model.IntrospectionResponse;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@RestController
public class TokenIntrospectionController {

  private TokenIntrospectionService tokenIntrospectionService;

  public TokenIntrospectionController(TokenIntrospectionService tokenIntrospectionService) {

    this.tokenIntrospectionService = tokenIntrospectionService;
  }

  @PostMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE)
  public IntrospectionResponse introspectToken(
      @RequestParam(value = OAuth2ParameterNames.TOKEN, required = true) String accessToken)
      throws TokenIntrospectionException {

    return tokenIntrospectionService.introspect(accessToken);
  }
}
