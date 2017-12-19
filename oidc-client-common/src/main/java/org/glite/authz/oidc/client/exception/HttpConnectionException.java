package org.glite.authz.oidc.client.exception;

public class HttpConnectionException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public HttpConnectionException(String message) {

    super(message);
  }

  public HttpConnectionException(String message, Throwable cause) {

    super(message, cause);
  }
}
