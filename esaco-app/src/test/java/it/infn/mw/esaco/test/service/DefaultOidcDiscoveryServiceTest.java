package it.infn.mw.esaco.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import it.infn.mw.esaco.service.OidcDiscoveryService;
import it.infn.mw.esaco.service.impl.DefaultOidcDiscoveryService;

class DefaultOidcDiscoveryServiceTest {

  private RestTemplate restTemplate;
  private OidcDiscoveryService service;

  @BeforeEach
  void setUp() {
    restTemplate = mock(RestTemplate.class);
    service = new DefaultOidcDiscoveryService();
  }

  @Test
  void getDiscoveryDocumentValidOidcUrlReturnsJsonNode() throws Exception {
    String issuer = "https://example.com";
    String responseBody = "{\"issuer\":\"https://example.com\"}";

    when(restTemplate.getForEntity(issuer + "/.well-known/openid-configuration", String.class))
      .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    JsonNode result = service.getDiscoveryDocument(issuer, restTemplate);

    assertNotNull(result);
    assertEquals("https://example.com", result.get("issuer").asText());
  }

  @Test
  void getDiscoveryDocumentFirstUrlFailsSecondUrlSucceeds() throws Exception {
    String issuer = "https://issuer.com";
    String oauthResponse = "{\"token_endpoint\":\"https://issuer.com/token\"}";

    when(restTemplate.getForEntity(issuer + "/.well-known/openid-configuration", String.class))
      .thenThrow(new RestClientException("OIDC fail"));
    when(
        restTemplate.getForEntity(issuer + "/.well-known/oauth-authorization-server", String.class))
          .thenReturn(new ResponseEntity<>(oauthResponse, HttpStatus.OK));

    JsonNode result = service.getDiscoveryDocument(issuer, restTemplate);
    assertEquals("https://issuer.com/token", result.get("token_endpoint").asText());
  }

  @Test
  void getDiscoveryDocumentNoSuccessfulResponseThrowsException() {
    String issuer = "https://issuer.com";

    when(restTemplate.getForEntity(anyString(), eq(String.class)))
      .thenThrow(new RestClientException("All failed"));

    assertThrows(RestClientException.class,
        () -> service.getDiscoveryDocument(issuer, restTemplate));
  }

  @Test
  void getDiscoveryDocumentTrimsTrailingSlash() throws Exception {
    String issuer = "https://example.com/";
    String responseBody = "{\"issuer\":\"https://example.com\"}";

    when(restTemplate.getForEntity("https://example.com/.well-known/openid-configuration",
        String.class)).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

    JsonNode result = service.getDiscoveryDocument(issuer, restTemplate);
    assertEquals("https://example.com", result.get("issuer").asText());
  }
}
