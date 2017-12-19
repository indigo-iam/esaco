package org.glite.authz.oidc.client.utils;

import org.glite.authz.oidc.client.service.TimeProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockTimeProvider implements TimeProvider {

  private long currentTimeMillis = System.currentTimeMillis();

  @Override
  public long currentTimeMillis() {
    return currentTimeMillis;
  }

  public void setTime(long timeMillis) {
    currentTimeMillis = timeMillis;
  }

}
