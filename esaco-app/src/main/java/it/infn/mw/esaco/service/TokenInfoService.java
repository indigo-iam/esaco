package it.infn.mw.esaco.service;

import it.infn.mw.esaco.model.AccessToken;
import it.infn.mw.esaco.model.IamUser;

/***
 * 
 * Service to validate and get user information about an access token.
 *
 */
public interface TokenInfoService {

  /***
   * Parse a JWT access token and return its decoded representation
   * 
   * @param accessToken JWT access token to parse
   * @return Decoded access token
   */
  AccessToken parseJWTAccessToken(String accessToken);

  /***
   * Given a JWT access token, parse it and return introspection information
   * 
   * @param accessToken
   *          JWT access token
   * @return Token introspection json string
   */
  String introspectToken(String accessToken);

  /***
   * Given a JWT access token, parse it and return user details.
   * 
   * @param accessToken JWT access token
   * @return User details information
   */
  IamUser decodeUserInfo(String accessToken);

  /***
   * Given a JWT access token, check if the token is still active.
   * 
   * @param token JWT access token
   * @return <code>true</code> if the token is still active, <code>false</code> otherwise.
   */
  boolean isAccessTokenActive(AccessToken token);
}
