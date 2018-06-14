package it.infn.mw.esaco;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.JWTParsingIntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.SimpleIntrospectionAuthorityGranter;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticClientConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
import it.infn.mw.esaco.exception.SSLContextInitializationError;
import it.infn.mw.esaco.service.TimeProvider;
import it.infn.mw.esaco.service.impl.IamDynamicServerConfigurationService;
import it.infn.mw.esaco.service.impl.SystemTimeProvider;
import it.infn.mw.esaco.util.x509.X509BlindTrustManager;
import it.infn.mw.esaco.util.x509.X509BundleTrustManager;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class EsacoConfiguration {

  public enum TrustAnchorsType {dir, bundle, none};

  @Value("${x509.trustAnchorsDir}")
  private String trustAnchorsDir;

  @Value("${x509.trustAnchorsBundle}")
  private String trustAnchorsBundle;

  @Value("${x509.trustAnchorsType}")
  private TrustAnchorsType trustAnchorsType;

  @Value("${x509.trustAnchorsRefreshMsec}")
  private Long trustAnchorsRefreshInterval;

  @Value("${tls.version}")
  private String tlsVersion;

  @Autowired
  private OidcClientProperties oidcConfig;

  public SimpleIntrospectionAuthorityGranter authorityGranter() {

    return new SimpleIntrospectionAuthorityGranter();
  }

  @Bean
  public IntrospectionConfigurationService introspectionConfigService() {

    JWTParsingIntrospectionConfigurationService cs =
        new JWTParsingIntrospectionConfigurationService();
    cs.setServerConfigurationService(serverConfiguration());
    cs.setClientConfigurationService(oidcClientsConf());

    return cs;
  }

  @Bean
  @Primary
  public IntrospectingTokenService tokenService() {

    IntrospectingTokenService its = new IntrospectingTokenService();
    its.setIntrospectionConfigurationService(introspectionConfigService());
    its.setIntrospectionAuthorityGranter(authorityGranter());
    return its;
  }

  @Bean
  public ServerConfigurationService serverConfiguration() {

    return new IamDynamicServerConfigurationService(httpRequestFactory());
  }

  @Bean
  public ClientConfigurationService oidcClientsConf() {

    Map<String, RegisteredClient> clients = new LinkedHashMap<>();

    for (OidcClient client : oidcConfig.getClients()) {
      RegisteredClient rc = new RegisteredClient();
      rc.setClientId(client.getClientId());
      rc.setClientSecret(client.getClientSecret());
      clients.put(client.getIssuerUrl(), rc);
    }

    StaticClientConfigurationService conf = new StaticClientConfigurationService();
    conf.setClients(clients);
    return conf;
  }

  @Bean
  public X509CertChainValidatorExt certificateValidator() {

    return new CertificateValidatorBuilder().lazyAnchorsLoading(false)
      .trustAnchorsDir(trustAnchorsDir)
      .trustAnchorsUpdateInterval(trustAnchorsRefreshInterval)
      .build();
  }

  @Bean
  public X509TrustManager trustManager() throws KeyManagementException, CertificateException {

    switch(trustAnchorsType) {
      case dir:
        // reading trust anchors from a grid-style PEM directory
        return SocketFactoryCreator.getSSLTrustManager(certificateValidator());
      case bundle:
        // reading trust anchors from PEMs in a bundle, no CRLs
        return new X509BundleTrustManager(trustAnchorsBundle);
      case none:
        // blind & trusting, not for production use
        return new X509BlindTrustManager();
      default:
        throw new CertificateException("Unsupported trust anchors type: " + trustAnchorsType);
    }
  }

  @Bean
  public SSLContext sslContext() {

    try {
      SSLContext context = SSLContext.getInstance(tlsVersion);
      SecureRandom r = new SecureRandom();
      context.init(null, new TrustManager[] { trustManager() }, r);

      return context;

    } catch (NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
      throw new SSLContextInitializationError(e.getMessage(), e);
    }
  }

  @Bean
  public HttpClient httpClient() {

    SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext());

    Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
          .register("https", sf)
          .register("http", PlainConnectionSocketFactory.getSocketFactory())
          .build();

    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connectionManager.setMaxTotal(10);
    connectionManager.setDefaultMaxPerRoute(10);

    return HttpClientBuilder.create()
      .setConnectionManager(connectionManager)
      .disableAuthCaching()
      .build();
  }

  @Bean
  public ClientHttpRequestFactory httpRequestFactory() {

    return new HttpComponentsClientHttpRequestFactory(httpClient());
  }

  @Bean
  public RestTemplate restTemplate() {

    return new RestTemplate(httpRequestFactory());
  }

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {

    Jackson2ObjectMapperBuilder jacksonBuilder = new Jackson2ObjectMapperBuilder();
    jacksonBuilder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));
    return jacksonBuilder;
  }

  @Bean
  public TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }
}
