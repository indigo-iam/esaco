package org.glite.authz.oidc.client.service;

@FunctionalInterface
public interface TimeProvider {

  long currentTimeMillis();

}
