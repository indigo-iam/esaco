package it.infn.mw.esaco.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.infn.mw.esaco.exception.ErrorResponse;
import it.infn.mw.esaco.exception.HttpConnectionException;
import it.infn.mw.esaco.exception.TokenIntrospectionException;
import it.infn.mw.esaco.exception.TokenValidationException;
import it.infn.mw.esaco.exception.UnsupportedIssuerException;

@ControllerAdvice
public class EsacoExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TokenValidationException.class)
  @ResponseBody
  public ErrorResponse handleValidationException(TokenValidationException e) {

    return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TokenIntrospectionException.class)
  @ResponseBody
  public ErrorResponse handleIntrospectionException(TokenIntrospectionException e) {

    return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnsupportedIssuerException.class)
  @ResponseBody
  public ErrorResponse handleUnsupporedIssuerException(UnsupportedIssuerException e) {

    return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(HttpConnectionException.class)
  @ResponseBody
  public ErrorResponse handleHttpException(HttpConnectionException e) {

    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ErrorResponse buildErrorResponse(HttpStatus status, String message) {

    return new ErrorResponse(status.value(), status.getReasonPhrase(), message);
  }

}
