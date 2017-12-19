package org.glite.authz.oidc.client.exception;

public class TokenIntrospectionException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public TokenIntrospectionException(String message) {

    super(message);
  }

  public TokenIntrospectionException(String message, Throwable cause) {

    super(message, cause);
  }
}
