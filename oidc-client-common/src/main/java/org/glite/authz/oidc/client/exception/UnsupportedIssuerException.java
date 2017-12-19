package org.glite.authz.oidc.client.exception;

public class UnsupportedIssuerException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public UnsupportedIssuerException(String message) {

    super(message);
  }

}
