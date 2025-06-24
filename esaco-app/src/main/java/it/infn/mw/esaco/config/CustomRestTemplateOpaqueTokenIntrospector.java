package it.infn.mw.esaco.config;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class CustomRestTemplateOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final String introspectionUri;
  private final String clientId;
  private final String clientSecret;
  private final RestTemplate restTemplate;

  public CustomRestTemplateOpaqueTokenIntrospector(String introspectionUri, String clientId,
      String clientSecret, RestTemplate restTemplate) {
    this.introspectionUri = introspectionUri;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.restTemplate = restTemplate;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token)
      throws OAuth2AuthenticationException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", token);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<Map<String, Object>> response;

    try {
      response = restTemplate.exchange(introspectionUri, HttpMethod.POST, request,
          new ParameterizedTypeReference<Map<String, Object>>() {});

    } catch (RestClientException e) {
      throw new OAuth2AuthenticationException("Introspection endpoint call failed" + e);
    }

    return new DefaultOAuth2AuthenticatedPrincipal(response.getBody(),
        AuthorityUtils.NO_AUTHORITIES);
  }
}
