package it.infn.mw.esaco;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import eu.emi.security.authn.x509.ProxySupport;
import eu.emi.security.authn.x509.X509CertChainValidator;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.RevocationParametersExt;
import eu.emi.security.authn.x509.impl.ValidatorParamsExt;
import it.infn.mw.esaco.config.DelegatingOpaqueTokenIntrospector;
import it.infn.mw.esaco.exception.SSLContextInitializationError;
import it.infn.mw.esaco.service.TimeProvider;
import it.infn.mw.esaco.service.impl.SystemTimeProvider;
import it.infn.mw.esaco.util.x509.X509BlindTrustManager;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(OidcClientProperties.class)
public class EsacoConfiguration {

  private static final int TRUST_ANCHORS_BUNDLE_CONNECTION_TIMEOUT_CA_MSEC = 0; // 0 means no
  // timeout

  @Autowired
  private X509TrustProperties x509Properties;

  @Autowired
  private TlsProperties tlsProperties;

  @Bean
  X509CertChainValidatorExt certificateValidator() {

    return new CertificateValidatorBuilder().lazyAnchorsLoading(false)
      .trustAnchorsDir(x509Properties.getTrustAnchorsDir())
      .trustAnchorsUpdateInterval(x509Properties.getTrustAnchorsRefreshMsec())
      .build();
  }

  @Bean
  X509CertChainValidatorExt bundleValidator() throws CertificateException {

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
  X509TrustManager trustManager() throws CertificateException {
    X509CertChainValidator validator;
    switch (x509Properties.getTrustAnchorsType()) {
      case DIR:
        validator = certificateValidator();
        break;
      case BUNDLE:
        validator = bundleValidator();
        break;
      case NONE:
        return new X509BlindTrustManager();
      default:
        throw new CertificateException(
            "Unsupported trust anchors type: " + x509Properties.getTrustAnchorsType());
    }

    return new X509TrustManager() {
      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
        validator.validate(chain);
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
        validator.validate(chain);
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }
    };
  }

  @Bean
  SSLContext sslContext() {

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
  HttpClient httpClient() throws Exception {
    DefaultClientTlsStrategy sslSocketFactory = new DefaultClientTlsStrategy(sslContext());

    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
          .setTlsSocketStrategy(sslSocketFactory)
          .build();

    connectionManager.setMaxTotal(10);
    connectionManager.setDefaultMaxPerRoute(10);
    connectionManager
      .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofSeconds(30)).build());

    return HttpClients.custom()
      .setConnectionManager(connectionManager)
      .disableAuthCaching()
      .build();
  }

  @Bean
  ClientHttpRequestFactory httpRequestFactory() throws Exception {
    return new HttpComponentsClientHttpRequestFactory(httpClient());
  }

  @Bean
  RestTemplate restTemplate() throws Exception {
    return new RestTemplate(httpRequestFactory());
  }

  @Bean
  Jackson2ObjectMapperBuilder jacksonBuilder() {

    Jackson2ObjectMapperBuilder jacksonBuilder = new Jackson2ObjectMapperBuilder();
    jacksonBuilder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));
    return jacksonBuilder;
  }

  @Bean
  TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }

  @Bean
  OpaqueTokenIntrospector introspector(OidcClientProperties props, RestTemplate restTemplate) {
    return new DelegatingOpaqueTokenIntrospector(props, restTemplate);
  }
}
