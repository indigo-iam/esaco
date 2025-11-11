package it.infn.mw.esaco.service.impl;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.service.OidcDiscoveryService;

@Service
public class DefaultOidcDiscoveryService implements OidcDiscoveryService {

  private static final String OIDC_DISCOVERY_URL = "/.well-known/openid-configuration";
  private static final String OAUTH_DISCOVERY_URL = "/.well-known/oauth-authorization-server";
  private static final List<String> DISCOVERY_URLS =
      List.of(OIDC_DISCOVERY_URL, OAUTH_DISCOVERY_URL);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Cacheable(cacheNames = "oidcDiscovery", key = "#issuer")
  @Override
  public JsonNode getDiscoveryDocument(String issuer, RestTemplate restTemplate)
      throws RestClientException {

    String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;

    for (String url : DISCOVERY_URLS) {
      try {
        ResponseEntity<String> resp = restTemplate.getForEntity(base + url, String.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
          return objectMapper.readTree(resp.getBody());
        }
      } catch (Exception e) {
        // try the next one
      }
    }

    throw new RestClientException("Unable to discover OpenID configuration for issuer " + issuer);
  }

}
