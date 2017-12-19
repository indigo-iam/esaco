package org.glite.authz.oidc.client.service;

import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;

public interface TokenInfoService {

  AccessToken parseAccessToken(String accessToken);

  IamIntrospection introspectToken(String accessToken);

  IamUser decodeUserInfo(String accessToken);

  boolean isAccessTokenActive(AccessToken token);
}
