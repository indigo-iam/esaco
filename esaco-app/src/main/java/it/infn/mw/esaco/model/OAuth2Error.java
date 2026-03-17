package it.infn.mw.esaco.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class OAuth2Error {

  private final String error;
  private final String errorDescription;

  @JsonCreator
  public OAuth2Error(@JsonProperty("error") String error,
      @JsonProperty("error_description") String errorDescription) {

    this.error = error;
    this.errorDescription = errorDescription;
  }

  public String getError() {

    return error;
  }

  public String getErrorDescription() {

    return errorDescription;
  }
}
