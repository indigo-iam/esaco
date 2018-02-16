package org.glite.authz.oidc.client.service.impl;

import java.text.ParseException;
import java.util.Optional;

import org.glite.authz.oidc.client.exception.HttpConnectionException;
import org.glite.authz.oidc.client.exception.TokenIntrospectionException;
import org.glite.authz.oidc.client.exception.TokenValidationException;
import org.glite.authz.oidc.client.model.AccessToken;
import org.glite.authz.oidc.client.model.IamIntrospection;
import org.glite.authz.oidc.client.model.IamUser;
import org.glite.authz.oidc.client.service.TimeProvider;
import org.glite.authz.oidc.client.service.TokenInfoService;
import org.glite.authz.oidc.client.service.TokenIntrospectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

@Service
public class DefaultTokenInfoService implements TokenInfoService {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultTokenInfoService.class);

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private TokenIntrospectionService tokenIntrospectionService;

  @Autowired
  private TimeProvider timeProvider;

  @Override
  public AccessToken parseJWTAccessToken(String jwtAccessToken) {

    try {
      JWT jwt = JWTParser.parse(jwtAccessToken);

      String algorithm = jwt.getHeader().getAlgorithm().getName();
      String tokenIssuer = jwt.getJWTClaimsSet().getIssuer();
      String subject = jwt.getJWTClaimsSet().getSubject();
      Long expireAt = jwt.getJWTClaimsSet().getExpirationTime().getTime();
      Long issuedAt = jwt.getJWTClaimsSet().getIssueTime().getTime();
      String jwtTokenId = jwt.getJWTClaimsSet().getJWTID();

      return new AccessToken(algorithm, subject, tokenIssuer, expireAt, issuedAt, jwtTokenId);
    } catch (ParseException e) {
      LOGGER.error("Error decoding access token '{}': {}", jwtAccessToken, e.getMessage());
      throw new TokenValidationException("Error parsing access token: " + jwtAccessToken);
    }
  }

  @Override
  @Cacheable("introspect")
  public IamIntrospection introspectToken(String accessToken) {

    Optional<String> response = tokenIntrospectionService.introspectToken(accessToken);

    if (response.isPresent()) {
      try {
        return mapper.readValue(response.get(), IamIntrospection.class);
      } catch (Exception e) {
        String msg = "Error decoding information from introspection endpoint";
        LOGGER.error(msg, e);
        throw new TokenIntrospectionException(msg, e);
      }
    } else {
      throw new HttpConnectionException("Error connecting to introspection endpoint");
    }
  }

  @Override
  @Cacheable("userinfo")
  public IamUser decodeUserInfo(String accessToken) {

    IamUser info = null;
    Optional<String> response = tokenIntrospectionService.getUserInfoForToken(accessToken);

    if (response.isPresent()) {
      try {
        return mapper.readValue(response.get(), IamUser.class);
      } catch (Exception e) {
        String msg = "Error decoding information from userinfo endpoint";
        LOGGER.error(msg, e);
        throw new TokenIntrospectionException(msg, e);
      }
    } else {
      LOGGER.info("No userinfo data available for access token '{}'", accessToken);
    }
    return info;
  }

  @Override
  public boolean isAccessTokenActive(AccessToken token) {
    return token.getIssuedAt() <= timeProvider.currentTimeMillis()
        && timeProvider.currentTimeMillis() < token.getExpireAt();
  }
}
