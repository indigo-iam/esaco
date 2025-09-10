package it.infn.mw.esaco.exception;

public class InvalidClientCredentialsException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public InvalidClientCredentialsException(String message) {

    super(message);
  }

  public InvalidClientCredentialsException(String message, Throwable cause) {

    super(message, cause);
  }
}
