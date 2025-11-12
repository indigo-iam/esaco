package it.infn.mw.esaco.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.infn.mw.esaco.model.ErrorResponse;

@ControllerAdvice
public class EsacoExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(TokenIntrospectionException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleIntrospectionException(TokenIntrospectionException e) {

    Throwable cause = e.getCause();
    if (cause instanceof HttpClientErrorException.Unauthorized http401) {
      return buildErrorResponse(HttpStatus.UNAUTHORIZED, http401.getMessage());
    }
    if (cause instanceof HttpConnectionException httpConn) {
      return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, httpConn.getMessage());
    }
    if (cause instanceof DiscoveryDocumentNotFoundException discoveryEx) {
      return buildErrorResponse(HttpStatus.BAD_GATEWAY, discoveryEx.getMessage());
    }
    if (cause instanceof UnsupportedIssuerException basIssuer) {
      return buildErrorResponse(HttpStatus.BAD_REQUEST, basIssuer.getMessage());
    }
    if (cause instanceof TokenValidationException invalid) {
      return buildErrorResponse(HttpStatus.BAD_REQUEST, invalid.getMessage());
    }
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {

    ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
    return ResponseEntity.status(status).body(body);
  }
}
