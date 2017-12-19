package org.glite.authz.oidc.client;

import static java.lang.String.format;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.TokenIntrospectionException;
import org.glite.authz.oidc.client.exception.TokenValidationException;
import org.glite.authz.oidc.client.exception.UnsupportedIssuerException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.service.HttpService;
import org.glite.authz.oidc.client.service.TokenInfoService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.glite.authz.oidc.client.utils.MockTimeProvider;
import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
public class TokenInfoServiceTests extends ClientTestUtils {

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private TokenInfoService tokenService;

  @Autowired
  @Qualifier("mockTimeProvider")
  private MockTimeProvider timeProvider;

  @MockBean
  private HttpService httpService;

  @Test
  public void testIntrospectWithValidToken() throws Exception {

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(0);
    body.add("token", VALID_JWT);

    String expected = mapper.writeValueAsString(VALID_INTROSPECTION);

    given(httpService.postWithBasicAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any())).willReturn(expected);

    IamIntrospection introspection = tokenService.introspectToken(VALID_JWT);

    assertNotNull(introspection);

    assertThat(introspection.isActive(), is(true));
    assertThat(introspection.getUserId(), equalTo(USERNAME));
    assertThat(introspection.getClientId(), equalTo(CLIENT_ID));
    assertThat(introspection.getTokenType(), equalTo(TOKEN_TYPE));
    assertThat(introspection.getOrganisationName(), not(isEmptyOrNullString()));
  }

  @Test
  public void testUserInfoWithValidToken() throws Exception {

    String expected = mapper.writeValueAsString(VALID_USERINFO);

    given(httpService.postWithOAuthAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.any())).willReturn(expected);

    IamUser userinfo = tokenService.decodeUserInfo(VALID_JWT);

    assertNotNull(userinfo);

    assertThat(userinfo.getPreferredUsername(), equalTo(USERNAME));
    assertThat(userinfo.getGroups(), isA(String[].class));
    assertThat(userinfo.getGroups(), not(emptyArray()));
    assertThat(userinfo.getOrganisationName(), not(isEmptyOrNullString()));
  }

  @Test(expected = HttpConnectionException.class)
  public void testIntrospectionEndpointConnectionError() {

    given(httpService.postWithBasicAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any())).willThrow(
            new HttpConnectionException(format("Error connecting to endpoint '%s'", "foo")));

    try {
      IamIntrospection introspection = tokenService.introspectToken(VALID_JWT);
      assertNull(introspection);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = HttpConnectionException.class)
  public void testUserinfoEndpointConnectionError() {

    given(httpService
      .postWithOAuthAuthentication(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
        .willThrow(new HttpConnectionException(format("Error connecting to endpoint '%s'", "foo")));

    try {
      IamUser userInfo = tokenService.decodeUserInfo(VALID_JWT);
      assertNull(userInfo);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = TokenIntrospectionException.class)
  public void testIntrospectionParsingError() {

    given(httpService.postWithBasicAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any())).willReturn(
            "random_String}_that-isNot-a_JSON-representation_of:aIAM-.Introspection_object");

    try {
      IamIntrospection introspection = tokenService.introspectToken(VALID_JWT);
      assertNull(introspection);
    } catch (Exception e) {
      throw e;
    }

  }

  @Test(expected = TokenIntrospectionException.class)
  public void testUserinfoParsingError() {

    given(httpService.postWithOAuthAuthentication(Mockito.anyString(), Mockito.anyString(),
        Mockito.any()))
          .willReturn("Invalid_String}_that-isNot-a_JSON-representation_of:aIAM-.user^info_object");

    try {
      IamUser userinfo = tokenService.decodeUserInfo(VALID_JWT);
      assertNull(userinfo);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = UnsupportedIssuerException.class)
  public void testIntrospectionWithUnsupportedIssuer() {
    try {
      tokenService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = UnsupportedIssuerException.class)
  public void testuserInfoWithUnsupportedIssuer() {
    try {
      tokenService.decodeUserInfo(TOKEN_FROM_UNKNOWN_ISSUER);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = TokenValidationException.class)
  public void testIntrospectionWithNotJwtToken() {
    try {
      tokenService.introspectToken("any.notjwt.token");
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = TokenValidationException.class)
  public void testuserInfoWithNotJwtToken() {
    try {
      tokenService.decodeUserInfo("any.notjwt.token");
    } catch (Exception e) {
      throw e;
    }
  }

  @Test
  public void testAccessTokenIsActive() {

    AccessToken token = tokenService.parseAccessToken(VALID_JWT);
    assertNotNull(token);

    timeProvider.setTime(token.getIssuedAt());
    assertThat(tokenService.isAccessTokenActive(token), is(true));

    timeProvider.setTime(token.getIssuedAt() + 1);
    assertThat(tokenService.isAccessTokenActive(token), is(true));

    timeProvider.setTime(token.getIssuedAt() - 1);
    assertThat(tokenService.isAccessTokenActive(token), is(false));

    timeProvider.setTime(token.getExpireAt() - 1);
    assertThat(tokenService.isAccessTokenActive(token), is(true));

    timeProvider.setTime(token.getExpireAt());
    assertThat(tokenService.isAccessTokenActive(token), is(false));

    timeProvider.setTime(token.getExpireAt() + 1);
    assertThat(tokenService.isAccessTokenActive(token), is(false));
  }

}
