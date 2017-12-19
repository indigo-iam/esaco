package org.glite.authz.oidc.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oidc")
public class ClientProperties {
  private List<OidcClient> clients = new ArrayList<>();

  public List<OidcClient> getClients() {
    return clients;
  }
}
