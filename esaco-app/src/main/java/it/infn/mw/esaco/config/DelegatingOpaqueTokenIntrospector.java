package it.infn.mw.esaco.config;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.OidcClient;
import it.infn.mw.esaco.OidcClientProperties;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;

public class DelegatingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final OidcClientProperties properties;
  private final Function<OidcClient, RestTemplate> restTemplateFactory;
  private final Map<String, OpaqueTokenIntrospector> cache = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  public DelegatingOpaqueTokenIntrospector(OidcClientProperties properties,
      Function<OidcClient, RestTemplate> restTemplateFactory) {

    this.properties = properties;
    this.restTemplateFactory = restTemplateFactory;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {

    String issuer = getIssuerFromAccessToken(token);
    return cache.computeIfAbsent(issuer, this::createIntrospectorForIssuer).introspect(token);
  }

  private String getIssuerFromAccessToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken).getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      throw new TokenValidationException("Error reading issuer claim from access token", e);
    }
  }

  private OpaqueTokenIntrospector createIntrospectorForIssuer(String issuer) {

    OidcClient client = properties.getClients().stream()
        .filter(c -> issuer.equals(c.getIssuerUrl()))
        .findFirst()
        .orElseThrow(() -> new UnsupportedIssuerException("Unsupported issuer: " + issuer));

    RestTemplate restTemplate = restTemplateFactory.apply(client);
    String introspectionEndpoint = resolveIntrospectionEndpoint(issuer, restTemplate);

    return new SpringOpaqueTokenIntrospector(introspectionEndpoint, restTemplate);
  }

  /**
   * Discover the introspection_endpoint from the issuer's discovery document.
   * Tries OpenID Connect discovery first, then OAuth Authorization Server discovery.
   */
  private String resolveIntrospectionEndpoint(String issuerUrl, RestTemplate restTemplate) {

    // ensure issuerUrl does not end with a trailing slash
    String base = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;

    // candidate discovery endpoints (OIDC first, then oauth-authorization-server)
    List<String> discoveryUrls = Arrays.asList(
        base + "/.well-known/openid-configuration",
        base + "/.well-known/oauth-authorization-server"
    );

    for (String discoveryUrl : discoveryUrls) {
      try {
        ResponseEntity<String> resp = restTemplate.getForEntity(discoveryUrl, String.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
          continue;
        }
        JsonNode doc = objectMapper.readTree(resp.getBody());
        JsonNode introspectNode = doc.get("introspection_endpoint");
        if (introspectNode != null && introspectNode.isTextual()) {
          return introspectNode.asText();
        }
      } catch (RestClientException | java.io.IOException e) {
        // ignore this discovery URL and try the next; we'll throw later if none work
      }
    }

    throw new IllegalStateException(
        "Unable to discover introspection_endpoint for issuer: " + issuerUrl);
  }
}
