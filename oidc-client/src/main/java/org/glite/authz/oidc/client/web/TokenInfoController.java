package org.glite.authz.oidc.client.web;

import java.text.ParseException;

import org.glite.authz.oidc.client.exception.TokenValidationException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.model.TokenInfo;
import org.glite.authz.oidc.client.service.TokenInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTParser;

@RestController
public class TokenInfoController {

  @Autowired
  private TokenInfoService tokenInfoService;

  @RequestMapping(value = "/tokeninfo", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public TokenInfo getUserInfo(@RequestParam(name = "token", required = false) String accessToken) {

    accessTokenSanityChecks(accessToken);

    AccessToken token = tokenInfoService.parseAccessToken(accessToken);

    IamIntrospection introspection = null;
    IamUser info = null;

    if (tokenInfoService.isAccessTokenActive(token)) {
      introspection = tokenInfoService.introspectToken(accessToken);
      info = tokenInfoService.decodeUserInfo(accessToken);
    } else {
      introspection = IamIntrospection.getBuilder().isActive(false).build();
    }

    return new TokenInfo(token, introspection, info);
  }

  private void accessTokenSanityChecks(String accessToken) {

    if (Strings.isNullOrEmpty(accessToken)) {
      throw new TokenValidationException("Cannot perform request with empty token");
    }

    try {
      JWTParser.parse(accessToken);
    } catch (ParseException e) {
      throw new TokenValidationException("Malformed JWT token string", e);
    }
  }

}
