package it.infn.mw.esaco.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

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

  @Test
  public void testPostWithBasicAuthN() throws Exception {

    Map<String, Object> attributes = Map.of("sub", "user123", "active", true);

    DefaultOAuth2AuthenticatedPrincipal principal =
        new DefaultOAuth2AuthenticatedPrincipal(attributes, List.of());

    when(introspector.introspect(VALID_JWT)).thenReturn(principal);

    Optional<String> response = tokenIntrospectionService.introspectToken(VALID_JWT);

    assertThat(response).isPresent();
    assertThat(response.get()).isEqualTo(mapper.writeValueAsString(attributes));
  }

  /*
   * @Test public void testPostWithOAuth() throws Exception {
   * 
   * String expected = mapper.writeValueAsString(VALID_USERINFO);
   * 
   * when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
   * .thenReturn(expected);
   * 
   * Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);
   * 
   * assertThat(response.isPresent()).isTrue(); assertThat(response.get()).isNotBlank();
   * assertThat(response.get()).isEqualTo(expected); }
   */
  @Test
  public void testConnectionError() {
    when(introspector.introspect(VALID_JWT))
      .thenThrow(new OAuth2IntrospectionException("Connection refused"));

    Optional<String> result = tokenIntrospectionService.introspectToken(VALID_JWT);
    assertTrue(result.isEmpty(), "Expected empty Optional on connection error");
  }

  /*
   * @Test public void testUnauthorizedUserinfo() {
   * given(restTemplate.postForObject(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
   * .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
   * 
   * Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);
   * assertThat(response.isPresent()).isFalse(); }
   * 
   * @Test public void testForbiddenUserinfo() {
   * when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any()))
   * .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
   * 
   * Optional<String> response = tokenIntrospectionService.getUserInfoForToken(VALID_JWT);
   * assertThat(response.isPresent()).isFalse(); }
   */
  @Test
  public void testIntrospectionWithUnsupportedIssuer() {
    when(introspector.introspect(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new OAuth2IntrospectionException("Unsupported issuer"));

    assertThat(tokenIntrospectionService.introspectToken(TOKEN_FROM_UNKNOWN_ISSUER)).isEmpty();
  }
  /*
   * @Test public void testuserInfoWithUnsupportedIssuer() {
   * assertThrows(UnsupportedIssuerException.class, () -> {
   * tokenIntrospectionService.getUserInfoForToken(TOKEN_FROM_UNKNOWN_ISSUER); }); }
   */
}
