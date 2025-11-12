package it.infn.mw.esaco.exception;

public class DiscoveryDocumentNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DiscoveryDocumentNotFoundException(String message) {
    super(message);
  }
}
