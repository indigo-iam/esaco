package it.infn.mw.esaco.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class IamIntrospection {

  private boolean active;

  private final Map<String, Object> additionalFields = new HashMap<>();

  public IamIntrospection() {
    // Default constructor
  }

  public IamIntrospection(boolean active, Map<String, Object> additionalFields) {
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
    this.additionalFields.put(key, value);
  }

  public static class Builder {
    private final boolean active;
    private final Map<String, Object> additionalFields = new HashMap<>();

    public Builder(boolean active) {
      this.active = active;
    }

    public Builder addField(String key, Object value) {
      if (value != null && (!(value instanceof String) || !((String) value).isBlank())) {
        additionalFields.put(key, value);
      }
      return this;
    }

    public Builder addFields(Map<String, Object> fields) {
      fields.forEach(this::addField);
      return this;
    }

    public IamIntrospection build() {
      return new IamIntrospection(active, additionalFields);
    }
  }
}
