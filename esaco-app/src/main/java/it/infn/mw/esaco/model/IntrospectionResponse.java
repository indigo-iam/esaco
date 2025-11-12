package it.infn.mw.esaco.model;

import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.ACTIVE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.infn.mw.esaco.exception.TokenIntrospectionException;

@JsonInclude(Include.NON_EMPTY)
public class IntrospectionResponse {

  private boolean active;

  private final Map<String, Object> additionalFields = new HashMap<>();

  public IntrospectionResponse(OAuth2AuthenticatedPrincipal auth) {

    if (Objects.isNull(auth)) {
      throw new TokenIntrospectionException("Null OAuth2AuthenticatedPrincipal object");
    }
    if (Objects.isNull(auth.getAttributes())) {
      throw new TokenIntrospectionException("Null OAuth2AuthenticatedPrincipal.getAttributes() object");
    }
    setActive(Boolean.TRUE.equals(auth.getAttributes().get(OAuth2TokenIntrospectionClaimNames.ACTIVE)));
    auth.getAttributes().forEach((k, v) -> addAdditionalField(k, v));
  }

  public IntrospectionResponse() {
    // Default constructor
  }

  public IntrospectionResponse(boolean active, Map<String, Object> additionalFields) {
    this.active = active;
    if (additionalFields != null) {
      this.additionalFields.putAll(additionalFields);
    }
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalFields() {
    return additionalFields;
  }

  @JsonAnySetter
  public void addAdditionalField(String key, Object value) {
    if (ACTIVE.equalsIgnoreCase(key)) {
      return;
    }
    this.additionalFields.put(key, value);
  }

  public static class Builder {
    private final boolean active;
    private final Map<String, Object> additionalFields = new HashMap<>();

    public Builder(boolean active) {
      this.active = active;
    }

    public Builder addField(String key, Object value) {
      if (ACTIVE.equalsIgnoreCase(key) || Objects.isNull(value)) {
        return this;
      }
      if (value instanceof String valueStr && valueStr.isBlank()) {
        return this;
      }
      additionalFields.put(key, value);
      return this;
    }

    public Builder addFields(Map<String, Object> fields) {
      fields.forEach(this::addField);
      return this;
    }

    public IntrospectionResponse build() {
      return new IntrospectionResponse(active, additionalFields);
    }
  }
}
