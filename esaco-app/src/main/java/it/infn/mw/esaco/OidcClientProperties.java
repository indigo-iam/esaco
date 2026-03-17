/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc")
public class OidcClientProperties {
  private List<OidcClient> clients = new ArrayList<>();

  public List<OidcClient> getClients() {
    return clients;
  }
}
