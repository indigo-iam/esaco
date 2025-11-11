package it.infn.mw.esaco.config;

import static java.lang.String.format;

import java.text.ParseException;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.OidcClient;
import it.infn.mw.esaco.OidcClientProperties;
import it.infn.mw.esaco.exception.DiscoveryDocumentNotFoundException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.OidcDiscoveryService;

public class DelegatingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final OidcClientProperties properties;
  private final Function<OidcClient, RestTemplate> restTemplateFactory;
  private final OidcDiscoveryService discoveryService;

  public DelegatingOpaqueTokenIntrospector(OidcClientProperties properties,
      Function<OidcClient, RestTemplate> restTemplateFactory,
      OidcDiscoveryService discoveryService) {

    this.properties = properties;
    this.restTemplateFactory = restTemplateFactory;
    this.discoveryService = discoveryService;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {

    String issuer = getIssuerFromAccessToken(token);

    OidcClient client = properties.getClients().stream()
        .filter(c -> issuer.equals(c.getIssuerUrl()))
        .findFirst()
        .orElseThrow(() -> new UnsupportedIssuerException("Unsupported issuer: " + issuer));

    RestTemplate restTemplate = restTemplateFactory.apply(client);

    JsonNode discoveryDoc = discoveryService.getDiscoveryDocument(issuer, restTemplate);
    String introspectionEndpoint = discoveryDoc.path("introspection_endpoint").asText(null);

    if (Objects.isNull(introspectionEndpoint)) {
      throw new DiscoveryDocumentNotFoundException(format("No introspection_endpoint in discovery document for %s", issuer));
    }

    OpaqueTokenIntrospector introspector = new SpringOpaqueTokenIntrospector(introspectionEndpoint, restTemplate);
    return introspector.introspect(token);
  }

  private String getIssuerFromAccessToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken).getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      throw new TokenValidationException("Error reading issuer claim from access token", e);
    }
  }
}
