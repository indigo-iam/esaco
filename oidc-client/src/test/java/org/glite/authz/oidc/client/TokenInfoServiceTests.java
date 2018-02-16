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
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.TokenIntrospectionException;
import org.glite.authz.oidc.client.exception.TokenValidationException;
import org.glite.authz.oidc.client.exception.UnsupportedIssuerException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.service.TokenInfoService;
import org.glite.authz.oidc.client.service.impl.DefaultTokenIntrospectionService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.glite.authz.oidc.client.utils.MockTimeProvider;
import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
public class TokenInfoServiceTests extends ClientTestUtils {

  private static final String NOT_JWT_TOKEN = "any.notjwt.token";

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  @Qualifier("mockTimeProvider")
  private MockTimeProvider timeProvider;

  @MockBean
  private DefaultTokenIntrospectionService introspectionService;

  @Autowired
  private TokenInfoService tokenService;

  @Before
  public void setup() throws Exception {

    when(introspectionService.introspectToken(VALID_JWT))
      .thenReturn(Optional.of(mapper.writeValueAsString(VALID_INTROSPECTION)));
    when(introspectionService.getUserInfoForToken(VALID_JWT))
      .thenReturn(Optional.of(mapper.writeValueAsString(VALID_USERINFO)));

    when(introspectionService.introspectToken(NOT_JWT_TOKEN)).thenCallRealMethod();
    when(introspectionService.getUserInfoForToken(NOT_JWT_TOKEN)).thenCallRealMethod();

    when(introspectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new UnsupportedIssuerException(
          String.format("Issuer %s not supported", TestConfig.TEST_ISSUER)));
    when(introspectionService.getUserInfoForToken(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new UnsupportedIssuerException(
          String.format("Issuer %s not supported", TestConfig.TEST_ISSUER)));
  }

  @Test
  public void testIntrospectWithValidToken() throws Exception {

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

    IamUser userinfo = tokenService.decodeUserInfo(VALID_JWT);

    assertNotNull(userinfo);

    assertThat(userinfo.getPreferredUsername(), equalTo(USERNAME));
    assertThat(userinfo.getGroups(), isA(String[].class));
    assertThat(userinfo.getGroups(), not(emptyArray()));
    assertThat(userinfo.getOrganisationName(), not(isEmptyOrNullString()));
  }

  @Test(expected = HttpConnectionException.class)
  public void testIntrospectionEndpointConnectionError() {

    when(introspectionService.introspectToken(Mockito.anyString())).thenReturn(Optional.empty());

    try {
      IamIntrospection introspection = tokenService.introspectToken(VALID_JWT);
      assertNull(introspection);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = HttpConnectionException.class)
  public void testUserinfoEndpointConnectionError() {

    when(introspectionService.getUserInfoForToken(Mockito.anyString()))
      .thenThrow(new HttpConnectionException(format("Error connecting to endpoint '%s'", "foo")));

    try {
      IamUser userInfo = tokenService.decodeUserInfo(VALID_JWT);
      assertNull(userInfo);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = TokenIntrospectionException.class)
  public void testIntrospectionParsingError() {

    when(introspectionService.introspectToken(Mockito.anyString())).thenReturn(Optional
      .of("random_String}_that-isNot-a_JSON-representation_of:aIAM-.Introspection_object"));

    try {
      IamIntrospection introspection = tokenService.introspectToken(VALID_JWT);
      assertNull(introspection);
    } catch (Exception e) {
      throw e;
    }

  }

  @Test(expected = TokenIntrospectionException.class)
  public void testUserinfoParsingError() {

    when(introspectionService.getUserInfoForToken(Mockito.anyString())).thenReturn(
        Optional.of("Invalid_String}_that-isNot-a_JSON-representation_of:aIAM-.user^info_object"));

    try {
      IamUser userinfo = tokenService.decodeUserInfo(VALID_JWT);
      assertNull(userinfo);
    } catch (Exception e) {
      throw e;
    }
  }

  @Test(expected = TokenValidationException.class)
  public void testParseNotJwtToken() {
    try {
      tokenService.parseJWTAccessToken("any.notjwt.token");
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

    AccessToken token = tokenService.parseJWTAccessToken(VALID_JWT);
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
