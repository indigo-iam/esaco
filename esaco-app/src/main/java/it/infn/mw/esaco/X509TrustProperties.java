package it.infn.mw.esaco;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("x509")
@Configuration
public class X509TrustProperties {


  public enum TrustAnchorsType {
    DIR, BUNDLE, NONE
  }

  boolean useJvmTrustAnchors;

  String trustAnchorsDir;

  String trustAnchorsBundle;

  TrustAnchorsType trustAnchorsType;

  long trustAnchorsRefreshMsec;

  public String getTrustAnchorsBundle() {
    return trustAnchorsBundle;
  }

  public String getTrustAnchorsDir() {
    return trustAnchorsDir;
  }

  public boolean isUseJvmTrustAnchors() {
    return useJvmTrustAnchors;
  }

  public void setTrustAnchorsBundle(String trustAnchorsBundle) {
    this.trustAnchorsBundle = trustAnchorsBundle;
  }

  public void setTrustAnchorsDir(String trustAnchorsDir) {
    this.trustAnchorsDir = trustAnchorsDir;
  }

  public void setUseJvmTrustAnchors(boolean useJvmTrustAnchors) {
    this.useJvmTrustAnchors = useJvmTrustAnchors;
  }

  public long getTrustAnchorsRefreshMsec() {
    return trustAnchorsRefreshMsec;
  }

  public void setTrustAnchorsRefreshMsec(long trustAnchorsRefreshMsec) {
    this.trustAnchorsRefreshMsec = trustAnchorsRefreshMsec;
  }

  public void setTrustAnchorsType(TrustAnchorsType trustAnchorsType) {
    this.trustAnchorsType = trustAnchorsType;
  }

  public TrustAnchorsType getTrustAnchorsType() {
    return trustAnchorsType;
  }

}
