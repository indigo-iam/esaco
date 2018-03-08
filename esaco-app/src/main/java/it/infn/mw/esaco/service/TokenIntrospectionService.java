package it.infn.mw.esaco.service;

import java.util.Optional;

/***
 * 
 * This service extract user information from OIDC Access Token returning them as JSON string. Given
 * an access token, extracts the issuer and get introspection or user details calling the
 * introspection or the userinfo endpoints.
 *
 */
public interface TokenIntrospectionService {

  /***
   * @param accessToken Access token to introspect.
   * 
   * @return JSON string with introspection information; empty when the introspection fails.
   */
  Optional<String> introspectToken(String accessToken);

  /***
   * 
   * @param accessToken Access token from which get the user details.
   * @return JSON string with user details; empty when the request fails.
   */
  Optional<String> getUserInfoForToken(String accessToken);

}
