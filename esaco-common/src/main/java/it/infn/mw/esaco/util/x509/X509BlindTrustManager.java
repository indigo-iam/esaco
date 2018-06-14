package it.infn.mw.esaco.util.x509;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/*
*
*
*/
public class X509BlindTrustManager implements X509TrustManager {
  @Override
  public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }

  @Override
  public void checkClientTrusted(X509Certificate[] certs, String authType) {}

  @Override
  public void checkServerTrusted(X509Certificate[] certs, String authType) {}
}
