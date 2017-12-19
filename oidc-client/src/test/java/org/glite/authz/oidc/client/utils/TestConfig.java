package org.glite.authz.oidc.client.utils;

import org.glite.authz.oidc.client.service.TimeProvider;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.Sets;

@Configuration
public class TestConfig {

  public static final String TEST_CLIENT_ID = "iam";
  public static final String TEST_ISSUER = "http://localhost:8080/";
  public static final String TEST_AUTHORIZATION_ENDPOINT_URI = "http://localhost:8080/authz";
  public static final String TEST_TOKEN_ENDPOINT_URI = "http://localhost:8080/token";
  public static final String TEST_INTROSPECTION_ENDPOINT_URI = "http://localhost:8080/introspect";
  public static final String TEST_USERINFO_ENDPOINT_URI = "http://localhost:8080/userinfo";
  public static final String TEST_REVOKE_ENDPOINT_URI = "http://localhost:8080/revoke";
  public static final String TEST_JWKS_URI = "http://localhost:8080/jwk";

  @Bean
  @Primary
  public ServerConfigurationService mockServerConfigurationService() {

    ServerConfiguration sc = new ServerConfiguration();
    sc.setIssuer(TEST_ISSUER);
    sc.setAuthorizationEndpointUri(TEST_AUTHORIZATION_ENDPOINT_URI);
    sc.setTokenEndpointUri(TEST_TOKEN_ENDPOINT_URI);
    sc.setJwksUri(TEST_JWKS_URI);
    sc.setIntrospectionEndpointUri(TEST_INTROSPECTION_ENDPOINT_URI);
    sc.setUserInfoUri(TEST_USERINFO_ENDPOINT_URI);
    sc.setRevocationEndpointUri(TEST_REVOKE_ENDPOINT_URI);

    ServerConfigurationService service = Mockito.mock(ServerConfigurationService.class);
    Mockito.when(service.getServerConfiguration(TEST_ISSUER)).thenReturn(sc);

    return service;
  }

  private RegisteredClient fakeRegisteredClient() {
    RegisteredClient rc = new RegisteredClient();
    rc.setTokenEndpointAuthMethod(AuthMethod.SECRET_BASIC);
    rc.setScope(Sets.newHashSet("openid profile email"));
    rc.setClientId(TEST_CLIENT_ID);

    return rc;
  }

  @Bean
  @Primary
  public ClientConfigurationService mockClientConfiguration() {

    ClientConfigurationService service = Mockito.mock(ClientConfigurationService.class);
    Mockito.when(service.getClientConfiguration(Mockito.any())).thenReturn(fakeRegisteredClient());

    return service;
  }

  @Bean
  @Primary
  public CacheManager caffeineCacheManager() {

    CaffeineCacheManager manager = new CaffeineCacheManager("introspect", "userinfo");
    manager.setCacheSpecification("maximumSize=0");

    return manager;
  }

  @Bean(name = "mockTimeProvider")
  @Primary
  public TimeProvider timeProvider() {
    return new MockTimeProvider();
  }

  @Bean
  @Primary
  public IntrospectionConfigurationService introspectionConfigService() {

    IntrospectionConfigurationService cs = Mockito.mock(IntrospectionConfigurationService.class);
    Mockito.when(cs.getIntrospectionUrl(Mockito.anyString()))
      .thenReturn(TEST_INTROSPECTION_ENDPOINT_URI);
    Mockito.when(cs.getClientConfiguration(Mockito.anyString())).thenReturn(fakeRegisteredClient());

    return cs;
  }

}
