package it.infn.mw.esaco.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.model.IntrospectionResponse;
import it.infn.mw.esaco.service.impl.DefaultTokenIntrospectionService;
import it.infn.mw.esaco.test.utils.EsacoTestUtils;

class DefaultTokenIntrospectionServiceTest extends EsacoTestUtils {

  private OpaqueTokenIntrospector introspector;
  private DefaultTokenIntrospectionService service;

  @BeforeEach
  void setUp() {
    introspector = mock(OpaqueTokenIntrospector.class);
    service = new DefaultTokenIntrospectionService(introspector);
  }

  @Test
  void introspectValidTokenReturnsResponse() throws Exception {

    when(introspector.introspect(VALID_JWT)).thenReturn(VALID_INTROSPECTION);

    IntrospectionResponse expectedResponse = new IntrospectionResponse(VALID_INTROSPECTION);

    IntrospectionResponse response = service.introspect(VALID_JWT);

    assertNotNull(response);
    assertEquals(expectedResponse.isActive(), response.isActive());
    assertEquals(expectedResponse.getAdditionalFields(), response.getAdditionalFields());
    verify(introspector).introspect(VALID_JWT);
  }

  @Test
  void introspectNullTokenThrowsTokenValidationException() {
    TokenIntrospectionException e =
        assertThrows(TokenIntrospectionException.class, () -> service.introspect(null));
    verifyNoInteractions(introspector);
    assertTrue(e.getCause() instanceof TokenValidationException);
    assertEquals("Cannot perform request with empty token", e.getCause().getMessage());
  }

  @Test
  void introspectEmptyTokenThrowsTokenValidationException() {
    TokenIntrospectionException e =
        assertThrows(TokenIntrospectionException.class, () -> service.introspect(""));
    verifyNoInteractions(introspector);
    assertTrue(e.getCause() instanceof TokenValidationException);
    assertEquals("Cannot perform request with empty token", e.getCause().getMessage());
  }

  @Test
  void introspectMalformedJwtThrowsTokenValidationException() {
    String malformed = "not-a-jwt";
    TokenIntrospectionException e =
        assertThrows(TokenIntrospectionException.class, () -> service.introspect(malformed));
    verifyNoInteractions(introspector);
    assertTrue(e.getCause() instanceof TokenValidationException);
    assertEquals("Malformed JWT token string", e.getCause().getMessage());
  }

  @Test
  void introspectIntrospectorThrowsExceptionWrapsInTokenIntrospectionException() {
    when(introspector.introspect(VALID_JWT)).thenThrow(new RuntimeException("Introspection failed"));

    TokenIntrospectionException e = assertThrows(TokenIntrospectionException.class, () -> service.introspect(VALID_JWT));
    assertTrue(e.getMessage().contains("Introspection failed"));
  }
}

