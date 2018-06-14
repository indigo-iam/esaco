package it.infn.mw.esaco.util.x509;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.net.ssl.X509TrustManager;

/*
*
*
*/
public class X509BundleTrustManager implements X509TrustManager {

  private static final String CERT_TYPE = "X.509";
  private static final String VALIDATOR_TYPE = "PKIX";

  private final CertificateFactory certificateFactory;
  private final CertPathValidator certPathValidator;
  private final List<TrustAnchor> bundleCAs;
  private final PKIXParameters validationParameters;

  public X509BundleTrustManager(String caBundlePath) throws CertificateException {
    try {
      certificateFactory = CertificateFactory.getInstance(CERT_TYPE);
      certPathValidator = CertPathValidator.getInstance(VALIDATOR_TYPE);

      bundleCAs = buildAnchors(caBundlePath, certificateFactory);
      validationParameters = buildValidationParameters(bundleCAs);
    } catch (IOException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
      throw new CertificateException(e.getMessage(), e);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    X509Certificate[] issuers = new X509Certificate[bundleCAs.size()];

    for (int i = 0; i < bundleCAs.size(); i++) {
        issuers[i] = bundleCAs.get(i).getTrustedCert();
    }

    return issuers;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
    checkServerTrusted(certs, authType);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
    CertPath certPath = certificateFactory.generateCertPath(Arrays.asList(certs));

    try {
      certPathValidator.validate(certPath, validationParameters);
    } catch (CertPathValidatorException | InvalidAlgorithmParameterException e) {
      Throwable t = e;
      while (t.getCause() != null) t = t.getCause(); // get root exception, more convenient
      throw new CertificateException(t.getMessage(), e);
    }
  }

  static private List<TrustAnchor> buildAnchors(String caBundlePath, CertificateFactory certificateFactory) throws
    CertificateException, UnsupportedEncodingException, IOException {

    List<TrustAnchor> anchors = new ArrayList<>();
    FileInputStream fisCaBundle = new FileInputStream(caBundlePath);
    BufferedInputStream bisCaBundle = new BufferedInputStream(fisCaBundle);

    // each call to generateCertificate consumes only one certificate, and the read position
    // of the input stream is positioned to the next certificate in the file
    while (bisCaBundle.available() > 0) {
      X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(bisCaBundle);
      TrustAnchor ca = new TrustAnchor(caCert, null);
      anchors.add(ca);
    }

    return anchors;
  }

  static private PKIXParameters buildValidationParameters(List<TrustAnchor> bundleCAs) throws
    InvalidAlgorithmParameterException {

    PKIXParameters params = new PKIXParameters(new HashSet<>(bundleCAs));
    params.setRevocationEnabled(false); // disable CRL checking since we are not supplying any CRLs

    return params;
  }


}
