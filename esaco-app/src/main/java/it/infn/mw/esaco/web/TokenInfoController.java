package it.infn.mw.esaco.web;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.model.AccessToken;
//import it.infn.mw.esaco.model.IamIntrospection;
import it.infn.mw.esaco.model.IamUser;
import it.infn.mw.esaco.model.TokenInfo;
import it.infn.mw.esaco.service.TokenInfoService;

@RestController
public class TokenInfoController {

  @Autowired
  private TokenInfoService tokenInfoService;
  
  private String tokenNotActive = "{\"Active\":false\"}";

  @RequestMapping(value = "/tokeninfo", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public TokenInfo getUserInfo(@RequestParam(name = "token", required = false) String accessToken) {

    accessTokenSanityChecks(accessToken);

    AccessToken token = tokenInfoService.parseJWTAccessToken(accessToken);

    String introspection = null;
    IamUser info = null;

    if (tokenInfoService.isAccessTokenActive(token)) {
      introspection = tokenInfoService.introspectToken(accessToken);
      info = tokenInfoService.decodeUserInfo(accessToken);
    } else {
      introspection = tokenNotActive;
    }

    return new TokenInfo(token, introspection, info);
  }

  @RequestMapping(value = "/introspect", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String introspectToken(
      @RequestParam(name = "token", required = false) String accessToken) {
    accessTokenSanityChecks(accessToken);
    
    AccessToken token = tokenInfoService.parseJWTAccessToken(accessToken);
    
    if (!tokenInfoService.isAccessTokenActive(token)) {
      return tokenNotActive;
    }
    
    return tokenInfoService.introspectToken(accessToken); 
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
