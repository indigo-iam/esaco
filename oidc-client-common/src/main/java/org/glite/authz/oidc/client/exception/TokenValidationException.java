package org.glite.authz.oidc.client.exception;

public class TokenValidationException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public TokenValidationException(String message) {

    super(message);
  }

  public TokenValidationException(String message, Throwable cause) {

    super(message, cause);
  }
}
