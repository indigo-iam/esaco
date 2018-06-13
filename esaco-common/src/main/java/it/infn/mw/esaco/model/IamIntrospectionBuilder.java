package it.infn.mw.esaco.model;

public class IamIntrospectionBuilder {
  boolean active;
  String scope;
  String expiresAt;
  Long exp;
  String iss;
  String sub;
  String userId;
  String clientId;
  String tokenType;
  String[] groups;
  String preferredUsername;
  String organisationName;
  String name;
  String email;
  String[] groupNames;
  String[] eduPersonEntitlements;
  String acr;

  public IamIntrospectionBuilder isActive(boolean active) {
    this.active = active;
    return this;
  }

  public IamIntrospectionBuilder scope(String scope) {
    this.scope = scope;
    return this;
  }

  public IamIntrospectionBuilder expiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  public IamIntrospectionBuilder exp(Long exp) {
    this.exp = exp;
    return this;
  }

  public IamIntrospectionBuilder iss(String iss) {
    this.iss = iss;
    return this;
  }

  public IamIntrospectionBuilder sub(String sub) {
    this.sub = sub;
    return this;
  }

  public IamIntrospectionBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  public IamIntrospectionBuilder clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public IamIntrospectionBuilder tokenType(String tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  public IamIntrospectionBuilder groups(String[] groups) {
    this.groups = groups;
    return this;
  }

  public IamIntrospectionBuilder preferredUsername(String preferredUsername) {
    this.preferredUsername = preferredUsername;
    return this;
  }

  public IamIntrospectionBuilder organisationName(String organisationName) {
    this.organisationName = organisationName;
    return this;
  }

  public IamIntrospectionBuilder name(String name) {
    this.name = name;
    return this;
  }

  public IamIntrospectionBuilder email(String email) {
    this.email = email;
    return this;
  }

  public IamIntrospectionBuilder groupNames(String[] groupNames) {
    this.groupNames = groupNames;
    return this;
  }

  public IamIntrospectionBuilder eduPersonEntitlements(String[] eduPersonEntitlements) {
    this.eduPersonEntitlements = eduPersonEntitlements;
    return this;
  }

  public IamIntrospectionBuilder acr(String acr) {
    this.acr = acr;
    return this;
  }

  public IamIntrospection build() {
    return new IamIntrospection(this);
  }
}
