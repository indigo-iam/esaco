package it.infn.mw.esaco.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TokenInfo {

  private final AccessToken accessToken;
  private final String introspection;
  private final IamUser userinfo;

  @JsonCreator
  public TokenInfo(@JsonProperty("access_token") AccessToken accessToken,
    @JsonProperty("introspection") String introspection,
      @JsonProperty("userinfo") IamUser userinfo) {

    super();
    this.accessToken = accessToken;
    this.introspection = introspection;
    this.userinfo = userinfo;
  }

  public AccessToken getAccessToken() {

    return accessToken;
  }

  public String getIntrospection() {

    return introspection;
  }

  public IamUser getUserinfo() {

    return userinfo;
  }

}
