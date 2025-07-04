package it.infn.mw.esaco.test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.model.AccessToken;
import it.infn.mw.esaco.model.IamIntrospection;
import it.infn.mw.esaco.model.IamUser;
import it.infn.mw.esaco.model.TokenInfo;
import it.infn.mw.esaco.service.impl.DefaultTokenInfoService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

@SuppressWarnings("removal")
@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = "USER")
public class TokenInfoControllerTests extends EsacoTestUtils {

  final static String ENDPOINT = "/tokeninfo";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private DefaultTokenInfoService tokenInfoService;

  @BeforeEach
  public void setup() throws Exception {
    given(tokenInfoService.isAccessTokenActive(Mockito.any())).willReturn(true);

    given(tokenInfoService.parseJWTAccessToken(Mockito.anyString())).willCallRealMethod();

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

    assertThat(accessToken.getAlgorithm()).isEqualTo(ALG);
    assertThat(accessToken.getIssuer()).isEqualTo(ISS);
    assertThat(accessToken.getSubject()).isEqualTo(SUB);

    assertThat(introspection.isActive()).isTrue();
    assertThat(introspection.getAdditionalFields().get("user_id")).isEqualTo(USERNAME);
    assertThat(introspection.getAdditionalFields().get("client_id")).isEqualTo(CLIENT_ID);
    assertThat(introspection.getAdditionalFields().get("token_type")).isEqualTo(TOKEN_TYPE);
    assertThat(introspection.getAdditionalFields().get("organisation_name")).isNotNull();
    assertThat(introspection.getAdditionalFields().get("entitlements")).isNotNull();
    assertThat(introspection.getAdditionalFields().get("eduperson_entitlement")).isNotNull();
    assertThat(introspection.getAdditionalFields().get("acr")).isNotNull();

    assertThat(userinfo.getAdditionalFields().get("preferred_username")).isEqualTo(USERNAME);
    assertThat(userinfo.getAdditionalFields().get("groups")).isNotNull();
    assertThat(userinfo.getAdditionalFields().get("organisation_name")).isNotNull();
    assertThat(userinfo.getAdditionalFields().get("entitlements")).isNotNull();
    assertThat(userinfo.getAdditionalFields().get("eduperson_entitlement")).isNotNull();
    assertThat(userinfo.getAdditionalFields().get("acr")).isNotNull();
  }

  @Test
  public void testGetInfoWithExpiredToken() throws Exception {
    given(tokenInfoService.isAccessTokenActive(Mockito.any())).willReturn(false);

    mvc.perform(post(ENDPOINT).param("token", EXPIRED_JWT))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.introspection").exists())
      .andExpect(jsonPath("$.introspection.active", equalTo(false)));
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

    assertThat(accessToken.getAlgorithm()).isEqualTo(ALG);
    assertThat(accessToken.getIssuer()).isEqualTo(ISS);
    assertThat(accessToken.getSubject()).isEqualTo("client-cred");

    IamIntrospection introspection = tokenInfo.getIntrospection();


    assertThat(introspection.isActive()).isTrue();
    assertThat(introspection.getAdditionalFields().get("client_id")).isEqualTo("client-cred");
    assertThat(introspection.getAdditionalFields().get("token_type")).isEqualTo(TOKEN_TYPE);
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
