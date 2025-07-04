package it.infn.mw.esaco.config;

import java.text.ParseException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.OidcClient;
import it.infn.mw.esaco.OidcClientProperties;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;

public class DelegatingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final Map<String, OpaqueTokenIntrospector> introspectors;

  public DelegatingOpaqueTokenIntrospector(OidcClientProperties properties,
      Function<OidcClient, RestTemplate> restTemplateFactory) {
    this.introspectors =
        properties.getClients()
          .stream()
          .collect(
              Collectors
                .toMap(OidcClient::getIssuerUrl,
                    client -> new SpringOpaqueTokenIntrospector(
                        client.getIssuerUrl() + "/introspect", restTemplateFactory.apply(client)),
                    (existing, replacement) -> existing));
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    String issuer = getIssuerFromAccessToken(token);
    OpaqueTokenIntrospector introspector = introspectors.get(issuer);
    if (introspector == null) {
      throw new UnsupportedIssuerException("No introspector for issuer: " + issuer);
    }
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
