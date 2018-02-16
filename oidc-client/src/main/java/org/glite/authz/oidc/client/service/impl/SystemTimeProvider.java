package org.glite.authz.oidc.client.service.impl;

import java.util.Calendar;

import org.glite.authz.oidc.client.service.TimeProvider;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {

  @Override
  public long currentTimeMillis() {

    return Calendar.getInstance().getTimeInMillis();
  }
}
