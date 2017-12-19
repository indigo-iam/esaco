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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.TokenIntrospectionException;
import org.glite.authz.oidc.client.exception.UnsupportedIssuerException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.model.TokenInfo;
import org.glite.authz.oidc.client.service.impl.DefaultTokenInfoService;
import org.glite.authz.oidc.client.utils.ClientTestUtils;
import org.glite.authz.oidc.client.utils.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ClientApplication.class, TestConfig.class})
@SpringBootTest
@AutoConfigureMockMvc
public class TokenInfoControllerTests extends ClientTestUtils {

  final static String ENDPOINT = "/tokeninfo";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private DefaultTokenInfoService tokenInfoService;

  @Before
  public void setup() {
    given(tokenInfoService.isAccessTokenActive(Mockito.any())).willReturn(true);

    given(tokenInfoService.parseAccessToken(Mockito.anyString())).willCallRealMethod();

    given(tokenInfoService.introspectToken(VALID_JWT)).willReturn(VALID_INTROSPECTION);
    given(tokenInfoService.decodeUserInfo(VALID_JWT)).willReturn(VALID_USERINFO);

    given(tokenInfoService.introspectToken(EXPIRED_JWT)).willReturn(EXPIRED_INTROSPECTION);

    given(tokenInfoService.introspectToken(CLIENT_CRED_JWT)).willReturn(CLIENT_CRED_INTROSPECTION);
    given(tokenInfoService.decodeUserInfo(CLIENT_CRED_JWT)).willReturn(null);

    given(tokenInfoService.introspectToken(TOKEN_WITH_PARSING_ERR)).willThrow(
        new TokenIntrospectionException("Error decoding information from introspection endpoint"));

    given(tokenInfoService.introspectToken(TOKEN_WITH_CONNECTION_ERR)).willThrow(
        new HttpConnectionException(String.format("Error connecting to endpoint: '%s'", "foo")));

    given(tokenInfoService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER))
      .willThrow(new UnsupportedIssuerException(
          String.format("Issuer '%s' not supported", UNSUPPORTED_ISSUER)));
  }

  @Test
  public void testGetInfoWithoutToken() throws Exception {

    mvc.perform(post(ENDPOINT))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())));
  }

  @Test
  public void testGetInfoWithNonJwtToken() throws Exception {

    String token = "abcdefghilmnopqrstuvz";

    mvc.perform(post(ENDPOINT).param("token", token))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").exists())
      .andExpect(jsonPath("$.status").value(equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())))
      .andExpect(jsonPath("$.message", equalTo("Malformed JWT token string")));
  }

  @Test
  public void testWithValidAccessToken() throws Exception {

    String response = mvc.perform(post(ENDPOINT).param("token", VALID_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    TokenInfo tokenInfo = mapper.readValue(response, TokenInfo.class);
    assertNotNull(tokenInfo);

    AccessToken accessToken = tokenInfo.getAccessToken();
    IamIntrospection introspection = tokenInfo.getIntrospection();
    IamUser userinfo = tokenInfo.getUserinfo();

    assertNotNull(accessToken);
    assertNotNull(introspection);
    assertNotNull(userinfo);

    assertThat(accessToken.getAlgorithm(), equalTo(ALG));
    assertThat(accessToken.getIssuer(), equalTo(ISS));
    assertThat(accessToken.getSubject(), equalTo(SUB));

    assertThat(introspection.isActive(), is(true));
    assertThat(introspection.getUserId(), equalTo(USERNAME));
    assertThat(introspection.getClientId(), equalTo(CLIENT_ID));
    assertThat(introspection.getTokenType(), equalTo(TOKEN_TYPE));
    assertThat(introspection.getOrganisationName(), not(isEmptyOrNullString()));

    assertThat(userinfo.getPreferredUsername(), equalTo(USERNAME));
    assertThat(userinfo.getGroups(), isA(String[].class));
    assertThat(userinfo.getGroups(), not(emptyArray()));
    assertThat(userinfo.getOrganisationName(), not(isEmptyOrNullString()));
  }

  @Test
  public void testGetInfoWithExpiredToken() throws Exception {
    given(tokenInfoService.isAccessTokenActive(Mockito.any())).willReturn(false);

    mvc.perform(post(ENDPOINT).param("token", EXPIRED_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.introspection").exists())
      .andExpect(jsonPath("$.introspection.active", is(false)));
  }

  @Test
  public void testGetInfoWithoutUserinfo() throws Exception {

    String response = mvc.perform(post(ENDPOINT).param("token", CLIENT_CRED_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    TokenInfo tokenInfo = mapper.readValue(response, TokenInfo.class);
    assertNotNull(tokenInfo);
    assertNotNull(tokenInfo.getAccessToken());
    assertNotNull(tokenInfo.getIntrospection());
    assertNull(tokenInfo.getUserinfo());

    AccessToken accessToken = tokenInfo.getAccessToken();

    assertThat(accessToken.getAlgorithm(), equalTo(ALG));
    assertThat(accessToken.getIssuer(), equalTo(ISS));
    assertThat(accessToken.getSubject(), equalTo("client-cred"));

    IamIntrospection introspection = tokenInfo.getIntrospection();

    assertThat(introspection.isActive(), is(true));
    assertThat(introspection.getClientId(), equalTo("client-cred"));
    assertThat(introspection.getTokenType(), equalTo(TOKEN_TYPE));
  }

  @Test
  public void testParsingError() throws Exception {

    mvc.perform(post(ENDPOINT).param("token", TOKEN_WITH_PARSING_ERR))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())))
      .andExpect(
          jsonPath("$.message", equalTo("Error decoding information from introspection endpoint")));
  }

  @Test
  public void testHttpConnectionError() throws Exception {

    mvc.perform(post(ENDPOINT).param("token", TOKEN_WITH_CONNECTION_ERR))
      .andDo(print())
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.status", equalTo(INTERNAL_SERVER_ERROR.value())))
      .andExpect(jsonPath("$.error", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase())))
      .andExpect(
          jsonPath("$.message", equalTo(format("Error connecting to endpoint: '%s'", "foo"))));
  }

  @Test
  public void testWithTokenFromUnsupportedIssuer() throws Exception {

    mvc.perform(post(ENDPOINT).param("token", TOKEN_FROM_UNKNOWN_ISSUER))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
      .andExpect(jsonPath("$.error", equalTo(BAD_REQUEST.getReasonPhrase())))
      .andExpect(
          jsonPath("$.message", equalTo(format("Issuer '%s' not supported", UNSUPPORTED_ISSUER))));
  }
}
