package it.infn.mw.esaco.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.EsacoApplication;
import it.infn.mw.esaco.service.OidcDiscoveryService;
import it.infn.mw.esaco.service.TokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

@ContextConfiguration(classes = {EsacoApplication.class})
@SpringBootTest
@ActiveProfiles("test")
public class TokenIntrospectionServiceTests extends EsacoTestUtils {

  @Autowired
  private ObjectMapper mapper;

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

    Optional<String> response = tokenIntrospectionService.introspect(VALID_JWT);

    assertTrue(response.isPresent());
    assertEquals(response.get(), mapper.writeValueAsString(attributes));
  }

  @Test
  public void testIntrospectConnectionError() {
    when(introspector.introspect(VALID_JWT))
      .thenThrow(new OAuth2IntrospectionException("Connection refused"));

    Optional<String> result = tokenIntrospectionService.introspect(VALID_JWT);
    assertTrue(result.isEmpty(), "Expected empty Optional on connection error");
  }

  @Test
  public void testIntrospectionWithUnsupportedIssuer() {
    when(introspector.introspect(TOKEN_FROM_UNKNOWN_ISSUER))
      .thenThrow(new OAuth2IntrospectionException("Unsupported issuer"));

    assertTrue(tokenIntrospectionService.introspect(TOKEN_FROM_UNKNOWN_ISSUER).isEmpty());
  }
}
