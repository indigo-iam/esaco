package it.infn.mw.esaco.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import it.infn.mw.esaco.model.OAuth2Error;
import it.infn.mw.esaco.model.ErrorType;

@RestControllerAdvice
public class EsacoExceptionHandler {

  @ExceptionHandler(TokenIntrospectionException.class)
  public ResponseEntity<OAuth2Error> handleTokenIntrospectionException(
      TokenIntrospectionException e) {
    Throwable cause = e.getCause();

    if (cause instanceof HttpClientErrorException.Unauthorized http401) {
      return buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorType.unauthorized_client,
          http401.getMessage());
    }
    if (cause instanceof HttpConnectionException httpConn) {
      return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.server_error,
          httpConn.getMessage());
    }
    if (cause instanceof DiscoveryDocumentNotFoundException discoveryEx) {
      return buildErrorResponse(HttpStatus.BAD_GATEWAY, ErrorType.server_error,
          discoveryEx.getMessage());
    }
    if (cause instanceof UnsupportedIssuerException unsupportedIssuer) {
      return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorType.invalid_token,
          unsupportedIssuer.getMessage());
    }
    if (cause instanceof TokenValidationException invalid) {
      return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorType.invalid_token,
          invalid.getMessage());
    }

    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.server_error,
        e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<OAuth2Error> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorType.invalid_request,
        "Required parameter '" + ex.getParameterName() + "' is not present.");
  }

  private ResponseEntity<OAuth2Error> buildErrorResponse(HttpStatus status, ErrorType error,
      String description) {
    OAuth2Error body = new OAuth2Error(error.name(), description);
    return ResponseEntity.status(status).body(body);
  }
}
