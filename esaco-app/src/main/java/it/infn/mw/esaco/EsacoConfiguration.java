package it.infn.mw.esaco;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Collections;
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

import eu.emi.security.authn.x509.ProxySupport;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.RevocationParametersExt;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
import eu.emi.security.authn.x509.impl.ValidatorParamsExt;
import it.infn.mw.esaco.exception.SSLContextInitializationError;
import it.infn.mw.esaco.service.TimeProvider;
import it.infn.mw.esaco.service.impl.IamDynamicServerConfigurationService;
import it.infn.mw.esaco.service.impl.SystemTimeProvider;
import it.infn.mw.esaco.util.x509.X509BlindTrustManager;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class EsacoConfiguration {

  private static final int TRUST_ANCHORS_BUNDLE_CONNECTION_TIMEOUT_CA_MSEC = 0; // 0 means no
  // timeout

  @Autowired
  private X509TrustProperties x509Properties;

  @Autowired
  private TlsProperties tlsProperties;

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
      .trustAnchorsDir(x509Properties.getTrustAnchorsDir())
      .trustAnchorsUpdateInterval(x509Properties.getTrustAnchorsRefreshMsec())
      .build();
  }

  @Bean
  public X509CertChainValidatorExt bundleValidator() throws CertificateException {

    ValidatorParamsExt validatorParams =
        new ValidatorParamsExt(RevocationParametersExt.IGNORE, ProxySupport.DENY);

    try {
      return new DirectoryCertChainValidator(
          Collections.singletonList(x509Properties.getTrustAnchorsBundle()), Encoding.PEM,
          x509Properties.getTrustAnchorsRefreshMsec(),
          TRUST_ANCHORS_BUNDLE_CONNECTION_TIMEOUT_CA_MSEC, null, validatorParams);
    } catch (KeyStoreException | IOException e) {
      throw new CertificateException(e.getMessage(), e);
    }
  }

  @Bean
  public X509TrustManager trustManager() throws CertificateException {

    switch (x509Properties.getTrustAnchorsType()) {
      case DIR:
        // reading trust anchors from a grid-style PEM directory
        return SocketFactoryCreator.getSSLTrustManager(certificateValidator());
      case BUNDLE:
        // reading trust anchors from PEMs in a bundle, no CRLs, no proxies
        return SocketFactoryCreator.getSSLTrustManager(bundleValidator());
      case NONE:
        // blind & trusting, not for production use
        return new X509BlindTrustManager();
      default:
        throw new CertificateException(
            "Unsupported trust anchors type: " + x509Properties.getTrustAnchorsType());
    }
  }

  @Bean
  public SSLContext sslContext() {

    try {
      SSLContext context = SSLContext.getInstance(tlsProperties.getVersion());
      SecureRandom r = new SecureRandom();

      if (x509Properties.isUseJvmTrustAnchors()) {
        context.init(null, null, r);
      } else {
        context.init(null, new TrustManager[] {trustManager()}, r);
      }

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
