package it.infn.mw.esaco.model;

import java.util.Arrays;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IamIntrospection {

  private final boolean active;
  private final String scope;
  private final String expiresAt;
  private final Long exp;
  private final String sub;
  private final String iss;
  private final String userId;
  private final String clientId;
  private final String tokenType;
  private final String[] groups;
  private final String preferredUsername;
  private final String organisationName;
  private final String name;
  private final String email;
  private final String[] groupNames;
  private final String[] eduPersonEntitlements;
  private final String acr;

  @JsonCreator
  public IamIntrospection(@JsonProperty("active") boolean active,
      @JsonProperty("scope") String scope, @JsonProperty("expires_at") String expiresAt,
      @JsonProperty("exp") Long exp, @JsonProperty("iss") String iss, @JsonProperty("sub") String sub,
      @JsonProperty("user_id") String userId, @JsonProperty("client_id") String clientId,
      @JsonProperty("token_type") String tokenType, @JsonProperty("groups") String[] groups,
      @JsonProperty("preferred_username") String preferredUsername,
      @JsonProperty("organisation_name") String organisationName,
      @JsonProperty("name") String name,
      @JsonProperty("email") String email,
      @JsonProperty("groupNames") String[] groupNames,
      @JsonProperty("edu_person_entitlements") String[] eduPersonEntitlements,
      @JsonProperty("acr") String acr) {

    this.active = active;
    this.scope = scope;
    this.expiresAt = expiresAt;
    this.exp = exp;
    this.iss = iss;
    this.sub = sub;
    this.userId = userId;
    this.clientId = clientId;
    this.tokenType = tokenType;
    this.groups = groups;
    this.preferredUsername = preferredUsername;
    this.organisationName = organisationName;
    this.name = name;
    this.email = email;
    this.groupNames = groupNames;
    this.eduPersonEntitlements = eduPersonEntitlements;
    this.acr = acr;
  }

  public IamIntrospection(IamIntrospectionBuilder builder) {
    this.active = builder.active;
    this.scope = builder.scope;
    this.expiresAt = builder.expiresAt;
    this.exp = builder.exp;
    this.iss = builder.iss;
    this.sub = builder.sub;
    this.userId = builder.userId;
    this.clientId = builder.clientId;
    this.tokenType = builder.tokenType;
    this.groups = builder.groups;
    this.preferredUsername = builder.preferredUsername;
    this.organisationName = builder.organisationName;
    this.name = builder.name;
    this.email = builder.email;
    this.groupNames = builder.groupNames;
    this.eduPersonEntitlements = builder.eduPersonEntitlements;
    this.acr = builder.acr;
  }

  public static IamIntrospectionBuilder getBuilder() {

    return new IamIntrospectionBuilder();
  }

  public boolean isActive() {

    return active;
  }


  public String getScope() {

    return scope;
  }


  public String getExpiresAt() {

    return expiresAt;
  }


  public Long getExp() {

    return exp;
  }


  public String getIss() {
    return iss;
  }


  public String getSub() {

    return sub;
  }


  public String getUserId() {

    return userId;
  }


  public String getClientId() {

    return clientId;
  }


  public String getTokenType() {

    return tokenType;
  }


  public String[] getGroups() {

    return groups;
  }


  public String getPreferredUsername() {

    return preferredUsername;
  }


  public String getOrganisationName() {

    return organisationName;
  }


  public String getName() {
    return name;
  }


  public String getEmail() {
    return email;
  }


  @JsonProperty("groupNames")
  public String[] getGroupNames() {
    return groupNames;
  }


  public String[] getEduPersonEntitlements() {
    return eduPersonEntitlements;
  }


  public String getAcr() {
    return acr;
  }


  @Generated("auto-generated method")
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((exp == null) ? 0 : exp.hashCode());
    result = prime * result + ((expiresAt == null) ? 0 : expiresAt.hashCode());
    result = prime * result + Arrays.hashCode(groups);
    result = prime * result + ((iss == null) ? 0 : iss.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((organisationName == null) ? 0 : organisationName.hashCode());
    result = prime * result + ((preferredUsername == null) ? 0 : preferredUsername.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    result = prime * result + ((sub == null) ? 0 : sub.hashCode());
    result = prime * result + ((tokenType == null) ? 0 : tokenType.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    result = prime * result + Arrays.hashCode(groupNames);
    result = prime * result + Arrays.hashCode(eduPersonEntitlements);
    result = prime * result + ((acr == null) ? 0 : acr.hashCode());
    return result;
  }

  @Generated("auto-generated method")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IamIntrospection other = (IamIntrospection) obj;
    if (active != other.active)
      return false;
    if (clientId == null) {
      if (other.clientId != null)
        return false;
    } else if (!clientId.equals(other.clientId))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (exp == null) {
      if (other.exp != null)
        return false;
    } else if (!exp.equals(other.exp))
      return false;
    if (expiresAt == null) {
      if (other.expiresAt != null)
        return false;
    } else if (!expiresAt.equals(other.expiresAt))
      return false;
    if (!Arrays.equals(groups, other.groups))
      return false;
    if (iss == null) {
      if (other.iss != null)
        return false;
    } else if (!iss.equals(other.iss))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (organisationName == null) {
      if (other.organisationName != null)
        return false;
    } else if (!organisationName.equals(other.organisationName))
      return false;
    if (preferredUsername == null) {
      if (other.preferredUsername != null)
        return false;
    } else if (!preferredUsername.equals(other.preferredUsername))
      return false;
    if (scope == null) {
      if (other.scope != null)
        return false;
    } else if (!scope.equals(other.scope))
      return false;
    if (sub == null) {
      if (other.sub != null)
        return false;
    } else if (!sub.equals(other.sub))
      return false;
    if (tokenType == null) {
      if (other.tokenType != null)
        return false;
    } else if (!tokenType.equals(other.tokenType))
      return false;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    if (!Arrays.equals(groupNames, other.groupNames))
      return false;
    if (!Arrays.equals(eduPersonEntitlements, other.eduPersonEntitlements))
      return false;
    if (acr == null) {
      if (other.acr != null)
        return false;
    } else if (!acr.equals(other.acr))
      return false;
    return true;
  }



}
