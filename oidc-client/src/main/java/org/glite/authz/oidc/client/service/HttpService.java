package org.glite.authz.oidc.client.service;

import org.springframework.util.MultiValueMap;

public interface HttpService {

  public String postWithBasicAuthentication(String endpoint, String username, String password,
      MultiValueMap<String, String> bodyParams);

  public String postWithOAuthAuthentication(String endpoint, String accessToken,
      MultiValueMap<String, String> bodyParams);

}
