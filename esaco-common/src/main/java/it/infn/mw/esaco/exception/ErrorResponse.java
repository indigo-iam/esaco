package it.infn.mw.esaco.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
@JsonFilter("attributeFilter")
public class ErrorResponse {

  private final Integer status;
  private final String error;
  private final String message;

  @JsonCreator
  public ErrorResponse(@JsonProperty("status") Integer status, @JsonProperty("error") String error,
      @JsonProperty("message") String message) {

    super();
    this.status = status;
    this.error = error;
    this.message = message;
  }

  public Integer getStatus() {

    return status;
  }

  public String getError() {

    return error;
  }

  public String getMessage() {

    return message;
  }
}
