package it.infn.mw.esaco.test.utils;

import java.util.Map;

import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticClientConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.infn.mw.esaco.service.TimeProvider;

@TestConfiguration
public class TestConfig {

  public static final String TEST_CLIENT_ID = "iam";
  public static final String TEST_ISSUER = "http://localhost:8080/";
  public static final String TEST_AUTHORIZATION_ENDPOINT_URI = "http://localhost:8080/authorize";
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

    Map<String, ServerConfiguration> servers = Maps.newLinkedHashMap();
    servers.put(TEST_ISSUER, sc);

    StaticServerConfigurationService service = new StaticServerConfigurationService();
    service.setServers(servers);

    return service;
  }

  @Bean(name = "fakeRegisteredClient")
  public RegisteredClient fakeRegisteredClient() {
    RegisteredClient rc = new RegisteredClient();
    rc.setTokenEndpointAuthMethod(AuthMethod.SECRET_BASIC);
    rc.setScope(Sets.newHashSet("openid profile email"));
    rc.setClientId(TEST_CLIENT_ID);

    return rc;
  }

  @Bean
  @Primary
  public ClientConfigurationService mockClientConfiguration() {

    Map<String, RegisteredClient> clients = Maps.newLinkedHashMap();
    clients.put(TEST_CLIENT_ID, fakeRegisteredClient());

    StaticClientConfigurationService service = new StaticClientConfigurationService();
    service.setClients(clients);

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

    StaticIntrospectionConfigurationService configurationService =
        new StaticIntrospectionConfigurationService();
    configurationService.setClientConfiguration(fakeRegisteredClient());
    configurationService.setIntrospectionUrl(TEST_INTROSPECTION_ENDPOINT_URI);

    return configurationService;
  }

}
