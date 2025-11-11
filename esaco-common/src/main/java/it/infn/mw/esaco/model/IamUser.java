package it.infn.mw.esaco.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.infn.mw.esaco.exception.TokenIntrospectionException;

@JsonInclude(Include.NON_NULL)
public class IamUser {

  private String sub;

  private final Map<String, Object> additionalFields = new HashMap<>();

  public IamUser() {
    // required for de-serialization
  }

  public IamUser(String sub, Map<String, Object> additionalFields) {
    this.sub = sub;
    if (additionalFields != null) {
      this.additionalFields.putAll(additionalFields);
    }
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalFields() {
    return additionalFields;
  }

  @JsonAnySetter
  public void addAdditionalField(String key, Object value) {
    this.additionalFields.put(key, value);
  }

  public static Builder builder(String sub) {
    return new Builder(sub);
  }

  public static class Builder {
    private final String sub;
    private final Map<String, Object> additionalFields = new HashMap<>();

    public Builder(String sub) {
      this.sub = sub;
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

    public IamUser build() {
      return new IamUser(sub, additionalFields);
    }
  }

  public static IamUser from(Optional<String> userInfoResponse) {

    if (userInfoResponse.isEmpty()) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(userInfoResponse.get(), IamUser.class);
    } catch (Exception e) {
      throw new TokenIntrospectionException("Error decoding information from userinfo endpoint", e);
    }
  }
}
