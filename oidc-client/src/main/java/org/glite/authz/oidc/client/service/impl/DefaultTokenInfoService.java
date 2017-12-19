package org.glite.authz.oidc.client.service.impl;

import java.text.ParseException;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.TokenIntrospectionException;
import org.glite.authz.oidc.client.exception.TokenValidationException;
import org.glite.authz.oidc.client.exception.UnsupportedIssuerException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.service.HttpService;
import org.glite.authz.oidc.client.service.TimeProvider;
import org.glite.authz.oidc.client.service.TokenInfoService;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

@Service
public class DefaultTokenInfoService implements TokenInfoService {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultTokenInfoService.class);

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private HttpService httpService;

  @Autowired
  private IntrospectionConfigurationService introspectionService;

  @Autowired
  private ServerConfigurationService serverConfig;

  @Autowired
  private TimeProvider timeProvider;

  @Override
  public AccessToken parseAccessToken(String accessToken) {

    AccessToken token = null;
    try {
      JWT jwt = JWTParser.parse(accessToken);

      String algorithm = jwt.getHeader().getAlgorithm().getName();
      String tokenIssuer = jwt.getJWTClaimsSet().getIssuer();
      String subject = jwt.getJWTClaimsSet().getSubject();
      Long expireAt = jwt.getJWTClaimsSet().getExpirationTime().getTime();
      Long issuedAt = jwt.getJWTClaimsSet().getIssueTime().getTime();
      String jwtTokenId = jwt.getJWTClaimsSet().getJWTID();

      token = new AccessToken(algorithm, subject, tokenIssuer, expireAt, issuedAt, jwtTokenId);
    } catch (ParseException e) {
      LOGGER.error("Error decoding access token '{}': {}", accessToken, e.getMessage());
      throw new TokenValidationException("Error parsing access token: " + accessToken);
    }

    return token;
  }

  @Override
  @Cacheable("introspect")
  public IamIntrospection introspectToken(String accessToken) {

    IamIntrospection introspect = null;

    String issuer = getIssuerFromAccessToken(accessToken);
    String endpoint = getServerConfiguration(issuer).getIntrospectionEndpointUri();

    RegisteredClient clientConfig = introspectionService.getClientConfiguration(accessToken);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", accessToken);

    String response = httpService.postWithBasicAuthentication(endpoint, clientConfig.getClientId(),
        clientConfig.getClientSecret(), body);

    if (response == null) {
      throw new HttpConnectionException("Error connecting to introspection endpoint");
    }

    try {
      introspect = mapper.readValue(response, IamIntrospection.class);
    } catch (Exception e) {
      String msg = "Error decoding information from introspection endpoint";
      LOGGER.error(msg, e);
      throw new TokenIntrospectionException(msg, e);
    }

    return introspect;
  }

  @Override
  @Cacheable("userinfo")
  public IamUser decodeUserInfo(String accessToken) {

    IamUser info = null;
    String issuer = getIssuerFromAccessToken(accessToken);
    String endpoint = getServerConfiguration(issuer).getUserInfoUri();

    String response = httpService.postWithOAuthAuthentication(endpoint, accessToken,
        new LinkedMultiValueMap<>(0));

    if (response == null) {
      LOGGER.info("No userinfo data available for access token '{}'", accessToken);
    } else {
      try {
        info = mapper.readValue(response, IamUser.class);
      } catch (Exception e) {
        String msg = "Error decoding information from userinfo endpoint";
        LOGGER.error(msg, e);
        throw new TokenIntrospectionException(msg, e);
      }
    }
    return info;
  }

  @Override
  public boolean isAccessTokenActive(AccessToken token) {
    return token.getIssuedAt() <= timeProvider.currentTimeMillis()
        && timeProvider.currentTimeMillis() < token.getExpireAt();
  }

  private String getIssuerFromAccessToken(String accessToken) {
    return parseAccessToken(accessToken).getIssuer();
  }

  private ServerConfiguration getServerConfiguration(String issuer) {
    ServerConfiguration server = serverConfig.getServerConfiguration(issuer);
    if (server == null) {
      LOGGER.error("Issuer '{}' not supported", issuer);
      throw new UnsupportedIssuerException(String.format("Issuer %s not supported", issuer));
    }
    return server;
  }
}
