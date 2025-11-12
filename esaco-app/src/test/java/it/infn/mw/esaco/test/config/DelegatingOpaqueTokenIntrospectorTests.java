package it.infn.mw.esaco.test.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.infn.mw.esaco.OidcClient;
import it.infn.mw.esaco.OidcClientProperties;
import it.infn.mw.esaco.config.DelegatingOpaqueTokenIntrospector;
import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.OidcDiscoveryService;

class DelegatingOpaqueTokenIntrospectorTests {

  public static final String OIDC_DISCOVERY_URL =
      "https://issuer.example.org/.well-known/openid-configuration";
  public static final String OAUTH_DISCOVERY_URL =
      "https://issuer.example.org/.well-known/oauth-authorization-server";
  public static final String UNTRUSTED_ISSUER = "https://unknown-issuer.example.org";

  private OidcClientProperties properties;
  private OidcClient client;
  private RestTemplate restTemplate;
  private Function<OidcClient, RestTemplate> restTemplateFactory;
  private DelegatingOpaqueTokenIntrospector delegatingIntrospector;
  private OidcDiscoveryService discoveryService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws JsonProcessingException {

    String issuer = "https://issuer.example.org";

    client = new OidcClient();
    client.setIssuerUrl(issuer);
    client.setClientId("client-id");
    client.setClientSecret("client-secret");

    properties = new OidcClientProperties();
    properties.getClients().add(client);

    restTemplate = mock(RestTemplate.class);
    restTemplateFactory = (Function<OidcClient, RestTemplate>) mock(Function.class);
    when(restTemplateFactory.apply(client)).thenReturn(restTemplate);

    discoveryService = mock(OidcDiscoveryService.class);
    addDiscoveryResponse(issuer, "https://issuer.example.org/introspect");

    delegatingIntrospector =
        new DelegatingOpaqueTokenIntrospector(properties, restTemplateFactory, discoveryService);
  }

  private void addDiscoveryResponse(String issuer, String discoveryResponse) throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();
    String json = "{ \"issuer\": \"" + issuer + "\" }";
    if (discoveryResponse != null) {
      json = "{ \"issuer\": \"" + issuer + "\", \"introspection_endpoint\": \""
          + discoveryResponse + "\" }";
    }
    JsonNode mockJsonNode = mapper.readTree(json);
    when(discoveryService.getDiscoveryDocument(issuer, restTemplate)).thenReturn(mockJsonNode);
  }

  @SuppressWarnings("unchecked")
  private void addIntrospectResponse(boolean isActive, HttpStatus responseStatus) {

    when(restTemplate.exchange(Mockito.any(RequestEntity.class),
        Mockito.any(ParameterizedTypeReference.class))).thenAnswer(invocation -> {
          Map<String, Object> body = new HashMap<>();
          body.put("active", isActive);
          return new ResponseEntity<>(body, responseStatus);
        });
  }

  private String createFakeJwtWithIssuer(String issuer) throws JOSEException {

    JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(issuer).subject("sub").build();
    SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner("12345678901234567890123456789012"));
    return jwt.serialize();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void testIntrospectionEndpointIsDiscoveredFromWellKnownConfiguration() throws Exception {

    String issuer = client.getIssuerUrl();
    String introspectUrl = "https://issuer.example.org/oauth2/introspect";

    addDiscoveryResponse(issuer, "https://issuer.example.org/oauth2/introspect");
    addIntrospectResponse(true, HttpStatus.OK);

    String token = createFakeJwtWithIssuer(issuer);
    delegatingIntrospector.introspect(token);

    ArgumentCaptor<RequestEntity> requestCaptor = ArgumentCaptor.forClass(RequestEntity.class);
    // Assert introspection was performed with correct URL
    verify(restTemplate).exchange(requestCaptor.capture(),
        Mockito.any(ParameterizedTypeReference.class));

    RequestEntity<?> capturedRequest = requestCaptor.getValue();
    assertNotNull(capturedRequest);
    assertEquals(introspectUrl, capturedRequest.getUrl().toString(),
        "SpringOpaqueTokenIntrospector should call the discovered introspection endpoint URL");
  }

  @Test
  void testUnsupportedIssuerThrowsException() throws Exception {

    String token = createFakeJwtWithIssuer(UNTRUSTED_ISSUER);

    UnsupportedIssuerException ex = assertThrows(UnsupportedIssuerException.class,
        () -> delegatingIntrospector.introspect(token));

    assertTrue(ex.getMessage().contains(UNTRUSTED_ISSUER));
  }

  @Test
  void testDiscoveryFailsWhenNoIntrospectionEndpoint() throws Exception {

    String issuer = client.getIssuerUrl();

    addDiscoveryResponse(issuer, null);
    addDiscoveryResponse(issuer , null);

    String token = createFakeJwtWithIssuer(issuer);

    assertThrows(DiscoveryDocumentNotFoundException.class,
        () -> delegatingIntrospector.introspect(token));
  }
}
