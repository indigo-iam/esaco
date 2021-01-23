package it.infn.mw.esaco.service.impl;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.text.ParseException;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWTParser;

import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;
import it.infn.mw.esaco.service.TokenIntrospectionService;

@Service
public class DefaultTokenIntrospectionService implements TokenIntrospectionService {

  public static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultTokenIntrospectionService.class);

  private static final Set<Integer> AUTH_ERROR_HTTP_CODES =
      Sets.newLinkedHashSet(Lists.newArrayList(FORBIDDEN.value(), UNAUTHORIZED.value()));


  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ServerConfigurationService serverConfig;

  @Autowired
  private IntrospectionConfigurationService introspectionConfigurationService;

  @Override
  public Optional<String> introspectToken(String accessToken) {

    RegisteredClient clientConfig;

    try {
      clientConfig = introspectionConfigurationService.getClientConfiguration(accessToken);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Error resolving client configuration: {}", e.getMessage());
      return Optional.empty();
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", accessToken);

    String plainCreds =
        String.format("%s:%s", clientConfig.getClientId(), clientConfig.getClientSecret());
    String base64Creds = new String(Base64.getEncoder().encode(plainCreds.getBytes()));

    HttpEntity<?> request = buildRequest("Basic " + base64Creds, body);

    String endpoint = introspectionConfigurationService.getIntrospectionUrl(accessToken);

    return postHttpRequest(request, endpoint);
  }

  @Override
  public Optional<String> getUserInfoForToken(String accessToken) {

    String issuer = getIssuerFromAccessToken(accessToken);
    String endpoint = getServerConfiguration(issuer).getUserInfoUri();

    HttpEntity<?> request = buildRequest("Bearer " + accessToken, new LinkedMultiValueMap<>(0));

    return postHttpRequest(request, endpoint);
  }

  private String getIssuerFromAccessToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken).getJWTClaimsSet().getIssuer();
    } catch (ParseException e) {
      throw new TokenValidationException("Error reading issuer claim from access token", e);
    }
  }

  private ServerConfiguration getServerConfiguration(String issuer) {
    ServerConfiguration server = serverConfig.getServerConfiguration(issuer);
    if (server == null) {
      LOGGER.error("Issuer '{}' not supported", issuer);
      throw new UnsupportedIssuerException(String.format("Issuer %s not supported", issuer));
    }
    return server;
  }

  private HttpEntity<?> buildRequest(String authorizationHeaderValue,
      MultiValueMap<String, String> bodyParams) {

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", authorizationHeaderValue);
    return new HttpEntity<>(bodyParams, headers);
  }

  private Optional<String> postHttpRequest(HttpEntity<?> request, String endpoint) {

    Optional<String> result = Optional.empty();
    try {
      result = Optional.ofNullable(restTemplate.postForObject(endpoint, request, String.class));
    } catch (HttpClientErrorException httpex) {
      if (AUTH_ERROR_HTTP_CODES.contains(httpex.getRawStatusCode())) {
        LOGGER.warn("Invalid authorization: '{}' '{}'", endpoint, httpex.getMessage());
      } else {
        LOGGER.error("HTTP error executing post request: '{}': {} {}", endpoint,
            httpex.getStatusCode(), httpex.getMessage());
      }
    } catch (RestClientException e) {
      LOGGER.error("Error connecting to endpoint: '{}' {}", endpoint, e.getMessage());
      throw new HttpConnectionException(format("Error connecting to endpoint '%s'", endpoint), e);
    }

    return result;
  }

}
