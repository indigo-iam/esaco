package it.infn.mw.esaco.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.service.TokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

@SuppressWarnings("removal")
@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenIntrospectionServiceTests extends EsacoTestUtils {

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private TokenIntrospectionService tokenIntrospectionService;

  @MockBean
  private OpaqueTokenIntrospector introspector;

  @MockBean
  private RestTemplate restTemplate;

  @Test
  public void testPostIntrospectForToken() throws Exception {

    Map<String, Object> attributes = Map.of("sub", "user123", "active", true);

    DefaultOAuth2AuthenticatedPrincipal principal =
        new DefaultOAuth2AuthenticatedPrincipal(attributes, List.of());

    when(introspector.introspect(VALID_JWT)).thenReturn(principal);

    Optional<String> response = tokenIntrospectionService.introspectToken(VALID_JWT);

    assertThat(response).isPresent();
    assertThat(response.get()).isEqualTo(mapper.writeValueAsString(attributes));
  }

  @Test
  public void testGetUserInfoForToken() throws Exception {

    Map<String, Object> wellKnownBody = new HashMap<>();
    wellKnownBody.put("userinfo_endpoint", "https://example.com/userinfo");

    ResponseEntity<Map<String, Object>> wellKnownResponse =
        new ResponseEntity<>(wellKnownBody, HttpStatus.OK);

    String expectedUserinfoJson = mapper.writeValueAsString(VALID_USERINFO);
    ResponseEntity<String> userinfoResponse =
        new ResponseEntity<>(expectedUserinfoJson, HttpStatus.OK);

    when(restTemplate.exchange(contains(".well-known/openid-configuration"), eq(HttpMethod.GET),
        isNull(), ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
          .thenReturn(wellKnownResponse);

    when(restTemplate.exchange(eq("https://example.com/userinfo"), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(String.class))).thenReturn(userinfoResponse);

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);

    assertThat(response).isPresent();
    assertThat(response.get()).isEqualTo(expectedUserinfoJson);
  }

  @Test
  public void testIntrospectConnectionError() {
    when(introspector.introspect(VALID_JWT))
      .thenThrow(new OAuth2IntrospectionException("Connection refused"));

    Optional<String> result = tokenIntrospectionService.introspectToken(VALID_JWT);
    assertTrue(result.isEmpty(), "Expected empty Optional on connection error");
  }

  @Test
  public void testUnauthorizedUserinfo() {
    Map<String, Object> wellKnownBody = new HashMap<>();
    wellKnownBody.put("userinfo_endpoint", "https://example.com/userinfo");
    ResponseEntity<Map<String, Object>> wellKnownResponse =
        new ResponseEntity<>(wellKnownBody, HttpStatus.OK);

    when(restTemplate.exchange(contains(".well-known/openid-configuration"), eq(HttpMethod.GET),
        isNull(), ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
          .thenReturn(wellKnownResponse);

    when(restTemplate.exchange(eq("https://example.com/userinfo"), eq(HttpMethod.GET),
        any(HttpEntity.class), eq(String.class)))
          .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);

    assertThat(response).isEmpty();
  }

  @Test
  public void testIntrospectionWithUnsupportedIssuer() {
    when(introspector.introspect(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new OAuth2IntrospectionException("Unsupported issuer"));

    assertThat(tokenIntrospectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER)).isEmpty();
  }
}
