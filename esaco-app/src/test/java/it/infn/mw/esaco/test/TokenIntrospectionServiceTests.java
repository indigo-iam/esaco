package it.infn.mw.esaco.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.model.IntrospectionResponse;
import it.infn.mw.esaco.service.TokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenIntrospectionServiceTests extends EsacoTestUtils {

  @Autowired
  private TokenIntrospectionService tokenIntrospectionService;

  @MockitoBean
  private OpaqueTokenIntrospector introspector;

  @Test
  public void testPostIntrospectForToken() throws Exception {

    Map<String, Object> attributes = Map.of("sub", "user123", "active", true);

    DefaultOAuth2AuthenticatedPrincipal principal =
        new DefaultOAuth2AuthenticatedPrincipal(attributes, List.of());

    when(introspector.introspect(VALID_JWT)).thenReturn(principal);

    IntrospectionResponse response = tokenIntrospectionService.introspect(VALID_JWT);

    assertNotNull(response);
    assertTrue(response.isActive());
    assertEquals(1, response.getAdditionalFields().size());
    assertEquals("user123", response.getAdditionalFields().get("sub"));
  }

  @Test
  public void testIntrospectConnectionError() {
    when(introspector.introspect(VALID_JWT))
      .thenThrow(new OAuth2IntrospectionException("Connection refused"));

    TokenIntrospectionException e = assertThrows(TokenIntrospectionException.class, () -> {
      tokenIntrospectionService.introspect(VALID_JWT);
    });
    assertTrue(e.getCause() instanceof OAuth2IntrospectionException);
  }

  @Test
  public void testIntrospectionWithUnsupportedIssuer() {
    when(introspector.introspect(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new UnsupportedIssuerException("Unsupported issuer"));

    TokenIntrospectionException e = assertThrows(TokenIntrospectionException.class, () -> {
      tokenIntrospectionService.introspect(TOKEN_FROM_UNKNOWN_ISSUER);
    });
    assertTrue(e.getCause() instanceof UnsupportedIssuerException);
  }
}
