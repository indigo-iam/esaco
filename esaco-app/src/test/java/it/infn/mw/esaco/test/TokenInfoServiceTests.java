package it.infn.mw.esaco.test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.model.AccessToken;
import it.infn.mw.esaco.model.IamIntrospection;
import it.infn.mw.esaco.model.IamUser;
import it.infn.mw.esaco.service.TokenInfoService;
import it.infn.mw.esaco.service.impl.SpringTokenIntrospectionAdapter;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;
import it.infn.mw.esaco.test.utils.MockTimeProvider;

@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenInfoServiceTests extends EsacoTestUtils {

  private static final String NOT_JWT_TOKEN = "any.notjwt.token";

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  @Qualifier("mockTimeProvider")
  private MockTimeProvider timeProvider;

  @MockitoBean
  private SpringTokenIntrospectionAdapter introspectionService;

  @Autowired
  private TokenInfoService tokenService;

  @BeforeEach
  public void setup() throws Exception {

    when(introspectionService.introspectToken(VALID_JWT))
      .thenReturn(Optional.of(mapper.writeValueAsString(VALID_INTROSPECTION)));
    when(introspectionService.getUserInfoForToken(VALID_JWT))
      .thenReturn(Optional.of(mapper.writeValueAsString(VALID_USERINFO)));

    when(introspectionService.introspectToken(NOT_JWT_TOKEN)).thenCallRealMethod();
    when(introspectionService.getUserInfoForToken(NOT_JWT_TOKEN)).thenCallRealMethod();

    when(introspectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new UnsupportedIssuerException(
          String.format("Issuer %s not supported", "http://localhost:8080/")));
    when(introspectionService.getUserInfoForToken(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new UnsupportedIssuerException(
          String.format("Issuer %s not supported", "http://localhost:8080/")));
  }

  @Test
  public void testIntrospectWithValidToken() throws Exception {

    String introspection = tokenService.introspectToken(VALID_JWT);

    assertThat(introspection).isNotNull();

    IamIntrospection iamIntrospection = mapper.readValue(introspection, IamIntrospection.class);

    assertThat(iamIntrospection.isActive()).isTrue();
    assertThat(iamIntrospection.getUserId()).isEqualTo(USERNAME);
    assertThat(iamIntrospection.getClientId()).isEqualTo(CLIENT_ID);
    assertThat(iamIntrospection.getTokenType()).isEqualTo(TOKEN_TYPE);
    assertThat(iamIntrospection.getOrganisationName()).isNotBlank();
    assertThat(iamIntrospection.getGroupNames()).isInstanceOf(String[].class).isNotEmpty();
    assertThat(iamIntrospection.getEduPersonEntitlements()).isInstanceOf(String[].class)
      .isNotEmpty();
    assertThat(iamIntrospection.getEduPersonEntitlement()).isInstanceOf(String[].class)
      .isNotEmpty();
    assertThat(iamIntrospection.getAcr()).isNotBlank();
  }

  @Test
  public void testUserInfoWithValidToken() throws Exception {

    IamUser userinfo = tokenService.decodeUserInfo(VALID_JWT);

    assertNotNull(userinfo);

    assertThat(userinfo.getPreferredUsername()).isEqualTo(USERNAME);
    assertThat(userinfo.getGroups()).isInstanceOf(String[].class).isNotEmpty();
    assertThat(userinfo.getOrganisationName()).isNotBlank();
    assertThat(userinfo.getGroupNames()).isInstanceOf(String[].class).isNotEmpty();
    assertThat(userinfo.getEduPersonEntitlements()).isInstanceOf(String[].class).isNotEmpty();
    assertThat(userinfo.getEduPersonEntitlement()).isInstanceOf(String[].class).isNotEmpty();
    assertThat(userinfo.getAcr()).isNotBlank();
  }

  @Test
  public void testIntrospectionEndpointConnectionError() {

    when(introspectionService.introspectToken(Mockito.anyString())).thenReturn(Optional.empty());

    assertThrows(HttpConnectionException.class, () -> {
      tokenService.introspectToken(VALID_JWT);
    });
  }

  @Test
  public void testUserinfoEndpointConnectionError() {

    when(introspectionService.getUserInfoForToken(Mockito.anyString()))
      .thenThrow(new HttpConnectionException(format("Error connecting to endpoint '%s'", "foo")));

    assertThrows(HttpConnectionException.class, () -> {
      tokenService.decodeUserInfo(VALID_JWT);
    });
  }

  @Test
  public void testIntrospectionParsingError() {

    when(introspectionService.introspectToken(Mockito.anyString())).thenReturn(Optional
      .of("random_String}_that-isNot-a_JSON-representation_of:aIAM-.Introspection_object"));

    assertThrows(TokenIntrospectionException.class, () -> {
      tokenService.introspectToken(VALID_JWT);
    });
  }

  @Test
  public void testUserinfoParsingError() {

    when(introspectionService.getUserInfoForToken(Mockito.anyString())).thenReturn(
        Optional.of("Invalid_String}_that-isNot-a_JSON-representation_of:aIAM-.user^info_object"));

    assertThrows(TokenIntrospectionException.class, () -> {
      tokenService.decodeUserInfo("broken_token_for_parsing_test");
    });
  }

  @Test
  public void testParseNotJwtToken() {

    assertThrows(TokenValidationException.class, () -> {
      tokenService.parseJWTAccessToken(NOT_JWT_TOKEN);
    });
  }

  @Test
  public void testAccessTokenIsActive() {

    AccessToken token = tokenService.parseJWTAccessToken(VALID_JWT);
    assertNotNull(token);

    timeProvider.setTime(token.getIssuedAt());
    assertThat(tokenService.isAccessTokenActive(token)).isTrue();

    timeProvider.setTime(token.getIssuedAt() + 1);
    assertThat(tokenService.isAccessTokenActive(token)).isTrue();

    timeProvider.setTime(token.getIssuedAt() - 1);
    assertThat(tokenService.isAccessTokenActive(token)).isFalse();

    timeProvider.setTime(token.getExpireAt() - 1);
    assertThat(tokenService.isAccessTokenActive(token)).isTrue();

    timeProvider.setTime(token.getExpireAt());
    assertThat(tokenService.isAccessTokenActive(token)).isFalse();

    timeProvider.setTime(token.getExpireAt() + 1);
    assertThat(tokenService.isAccessTokenActive(token)).isFalse();
  }
}
