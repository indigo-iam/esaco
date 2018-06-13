package it.infn.mw.esaco.model;

public class IamUserBuilder {
  String sub;
  String preferredUsername;
  String name;
  String givenName;
  String familyName;
  String middleName;
  String nickname;
  String profile;
  String picture;
  String website;
  String email;
  Boolean emailVerified;
  String gender;
  String zoneinfo;
  String locale;
  String phoneNumber;
  Boolean phoneNumberVerified;
  String address;
  String updatedAt;
  String birthdate;
  String[] groups;
  String organisationName;
  String[] groupNames;
  String[] eduPersonEntitlements;
  String acr;

  public IamUserBuilder sub(String sub) {
    this.sub = sub;
    return this;
  }

  public IamUserBuilder preferredUsername(String preferredUsername) {
    this.preferredUsername = preferredUsername;
    return this;
  }

  public IamUserBuilder name(String name) {
    this.name = name;
    return this;
  }

  public IamUserBuilder givenName(String givenName) {
    this.givenName = givenName;
    return this;
  }

  public IamUserBuilder familyName(String familyName) {
    this.familyName = familyName;
    return this;
  }

  public IamUserBuilder middleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public IamUserBuilder nickname(String nickname) {
    this.nickname = nickname;
    return this;
  }

  public IamUserBuilder profile(String profile) {
    this.profile = profile;
    return this;
  }

  public IamUserBuilder picture(String picture) {
    this.picture = picture;
    return this;
  }

  public IamUserBuilder website(String website) {
    this.website = website;
    return this;
  }

  public IamUserBuilder email(String email) {
    this.email = email;
    return this;
  }

  public IamUserBuilder emailVerified(Boolean emailVerifed) {
    this.emailVerified = emailVerifed;
    return this;
  }

  public IamUserBuilder gender(String gender) {
    this.gender = gender;
    return this;
  }

  public IamUserBuilder zoneinfo(String zoneinfo) {
    this.zoneinfo = zoneinfo;
    return this;
  }

  public IamUserBuilder locale(String locale) {
    this.locale = locale;
    return this;
  }

  public IamUserBuilder phoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  public IamUserBuilder phoneNumberVerified(Boolean phoneNUmberVerified) {
    this.phoneNumberVerified = phoneNUmberVerified;
    return this;
  }

  public IamUserBuilder address(String address) {
    this.address = address;
    return this;
  }

  public IamUserBuilder updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public IamUserBuilder birthdate(String birthdate) {
    this.birthdate = birthdate;
    return this;
  }

  public IamUserBuilder groups(String[] groups) {
    this.groups = groups;
    return this;
  }

  public IamUserBuilder organisationName(String organisationName) {
    this.organisationName = organisationName;
    return this;
  }

  public IamUserBuilder groupNames(String[] groupNames) {
    this.groupNames = groupNames;
    return this;
  }

  public IamUserBuilder eduPersonEntitlements(String[] eduPersonEntitlements) {
    this.eduPersonEntitlements = eduPersonEntitlements;
    return this;
  }

  public IamUserBuilder acr(String acr) {
    this.acr = acr;
    return this;
  }

  public IamUser build() {
    return new IamUser(this);
  }
}
