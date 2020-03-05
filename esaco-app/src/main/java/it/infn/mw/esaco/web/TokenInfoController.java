package it.infn.mw.esaco.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.infn.mw.esaco.model.AccessToken;
import it.infn.mw.esaco.model.IamUser;
import it.infn.mw.esaco.model.TokenInfo;
import it.infn.mw.esaco.service.TokenInfoService;

@RestController
public class TokenInfoController extends TokenControllerUtils {
  
  @Autowired
  private TokenInfoService tokenInfoService;
  
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
      introspection = INACTIVE_TOKEN_RESPONSE;
    }

    return new TokenInfo(token, introspection, info);
  }

}
