/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("tls")
@Configuration
public class TlsProperties {

  String version;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
