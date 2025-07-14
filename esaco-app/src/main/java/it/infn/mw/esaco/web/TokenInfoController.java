package it.infn.mw.esaco.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.infn.mw.esaco.model.AccessToken;
import it.infn.mw.esaco.model.IamIntrospection;
import it.infn.mw.esaco.model.IamUser;
import it.infn.mw.esaco.model.TokenInfo;
import it.infn.mw.esaco.service.TokenInfoService;

@RestController
public class TokenInfoController extends TokenControllerUtils {

  @Autowired
  private TokenInfoService tokenInfoService;

  @PostMapping(value = "/tokeninfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public TokenInfo getUserInfo(@RequestParam(name = "token", required = false) String accessToken) {

    accessTokenSanityChecks(accessToken);

    AccessToken token = tokenInfoService.parseJWTAccessToken(accessToken);

    IamIntrospection introspection = null;
    IamUser info = null;

    if (tokenInfoService.isAccessTokenActive(token)) {
      introspection = tokenInfoService.introspectToken(accessToken);
      info = tokenInfoService.decodeUserInfo(accessToken);
    } else {
      introspection = new IamIntrospection(false, null);
    }

    return new TokenInfo(token, introspection, info);
  }
}
