package it.infn.mw.esaco.model;

import java.util.Arrays;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IamUser {

  private final String sub;
  private final String preferredUsername;
  private final String name;
  private final String givenName;
  private final String familyName;
  private final String middleName;
  private final String nickname;
  private final String profile;
  private final String picture;
  private final String website;
  private final String email;
  private final Boolean emailVerified;
  private final String gender;
  private final String zoneinfo;
  private final String locale;
  private final String phoneNumber;
  private final Boolean phoneNumberVerified;
  private final String address;
  private final String updatedAt;
  private final String birthdate;
  private final String[] groups;
  private final String organisationName;
  private final String[] groupNames;
  private final String[] eduPersonEntitlements;
  private final String acr;

  @JsonCreator
  public IamUser(@JsonProperty("sub") String sub, @JsonProperty("name") String name,
      @JsonProperty("preferred_username") String preferredUsername,
      @JsonProperty("given_name") String givenName, @JsonProperty("family_name") String familyName,
      @JsonProperty("middle_name") String middleName, @JsonProperty("nickname") String nickname,
      @JsonProperty("gender") String gender, @JsonProperty("profile") String profile,
      @JsonProperty("picture") String picture, @JsonProperty("website") String website,
      @JsonProperty("email") String email, @JsonProperty("email_verified") Boolean emailVerified,
      @JsonProperty("zoneinfo") String zoneinfo, @JsonProperty("locale") String locale,
      @JsonProperty("phone_number") String phoneNumber,
      @JsonProperty("phone_number_verified") Boolean phoneNumberVerified,
      @JsonProperty("address") String address, @JsonProperty("updated_at") String updatedAt,
      @JsonProperty("birthdate") String birthdate, @JsonProperty("groups") String[] groups,
      @JsonProperty("organisation_name") String organisationName,
      @JsonProperty("groupNames") String[] groupNames,
      @JsonProperty("edu_person_entitlements") String[] eduPersonEntitlements,
      @JsonProperty("acr") String acr) {

    this.sub = sub;
    this.name = name;
    this.preferredUsername = preferredUsername;
    this.givenName = givenName;
    this.familyName = familyName;
    this.middleName = middleName;
    this.nickname = nickname;
    this.gender = gender;
    this.profile = profile;
    this.picture = picture;
    this.website = website;
    this.email = email;
    this.emailVerified = emailVerified;
    this.zoneinfo = zoneinfo;
    this.locale = locale;
    this.phoneNumber = phoneNumber;
    this.phoneNumberVerified = phoneNumberVerified;
    this.address = address;
    this.updatedAt = updatedAt;
    this.birthdate = birthdate;
    this.groups = groups;
    this.organisationName = organisationName;
    this.groupNames = groupNames;
    this.eduPersonEntitlements = eduPersonEntitlements;
    this.acr = acr;
  }

  public IamUser(IamUserBuilder builder) {
    this.sub = builder.sub;
    this.name = builder.name;
    this.preferredUsername = builder.preferredUsername;
    this.givenName = builder.givenName;
    this.familyName = builder.familyName;
    this.middleName = builder.middleName;
    this.nickname = builder.nickname;
    this.gender = builder.gender;
    this.profile = builder.profile;
    this.picture = builder.picture;
    this.website = builder.website;
    this.email = builder.email;
    this.emailVerified = builder.emailVerified;
    this.zoneinfo = builder.zoneinfo;
    this.locale = builder.locale;
    this.phoneNumber = builder.phoneNumber;
    this.phoneNumberVerified = builder.phoneNumberVerified;
    this.address = builder.address;
    this.updatedAt = builder.updatedAt;
    this.birthdate = builder.birthdate;
    this.groups = builder.groups;
    this.organisationName = builder.organisationName;
    this.groupNames = builder.groupNames;
    this.eduPersonEntitlements = builder.eduPersonEntitlements;
    this.acr = builder.acr;
  }

  public static IamUserBuilder getBuilder() {
    return new IamUserBuilder();
  }


  public String getSub() {

    return sub;
  }


  public String getPreferredUsername() {

    return preferredUsername;
  }


  public String getName() {

    return name;
  }


  public String getGivenName() {

    return givenName;
  }


  public String getFamilyName() {

    return familyName;
  }

  public String getMiddleName() {

    return middleName;
  }

  public String getNickname() {

    return nickname;
  }

  public String getProfile() {

    return profile;
  }

  public String getPicture() {

    return picture;
  }

  public String getWebsite() {

    return website;
  }

  public String getEmail() {

    return email;
  }

  public Boolean getEmailVerified() {

    return emailVerified;
  }

  public String getGender() {

    return gender;
  }

  public String getZoneinfo() {

    return zoneinfo;
  }

  public String getLocale() {

    return locale;
  }

  public String getPhoneNumber() {

    return phoneNumber;
  }

  public Boolean getPhoneNumberVerified() {

    return phoneNumberVerified;
  }

  public String getAddress() {

    return address;
  }

  public String getUpdatedAt() {

    return updatedAt;
  }

  public String getBirthdate() {

    return birthdate;
  }

  public String[] getGroups() {

    return groups;
  }

  public String getOrganisationName() {

    return organisationName;
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

  @Override
  @Generated("auto-generated method")
  public int hashCode() {

    int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((birthdate == null) ? 0 : birthdate.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((emailVerified == null) ? 0 : emailVerified.hashCode());
    result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
    result = prime * result + ((gender == null) ? 0 : gender.hashCode());
    result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
    result = prime * result + Arrays.hashCode(groups);
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
    result = prime * result + ((organisationName == null) ? 0 : organisationName.hashCode());
    result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
    result = prime * result + ((phoneNumberVerified == null) ? 0 : phoneNumberVerified.hashCode());
    result = prime * result + ((picture == null) ? 0 : picture.hashCode());
    result = prime * result + ((preferredUsername == null) ? 0 : preferredUsername.hashCode());
    result = prime * result + ((profile == null) ? 0 : profile.hashCode());
    result = prime * result + ((sub == null) ? 0 : sub.hashCode());
    result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
    result = prime * result + ((website == null) ? 0 : website.hashCode());
    result = prime * result + ((zoneinfo == null) ? 0 : zoneinfo.hashCode());
    result = prime * result + Arrays.hashCode(groupNames);
    result = prime * result + Arrays.hashCode(eduPersonEntitlements);
    result = prime * result + ((acr == null) ? 0 : acr.hashCode());
    return result;
  }

  @Override
  @Generated("auto-generated method")
  public boolean equals(Object obj) {

    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IamUser other = (IamUser) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (birthdate == null) {
      if (other.birthdate != null)
        return false;
    } else if (!birthdate.equals(other.birthdate))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (emailVerified == null) {
      if (other.emailVerified != null)
        return false;
    } else if (!emailVerified.equals(other.emailVerified))
      return false;
    if (familyName == null) {
      if (other.familyName != null)
        return false;
    } else if (!familyName.equals(other.familyName))
      return false;
    if (gender == null) {
      if (other.gender != null)
        return false;
    } else if (!gender.equals(other.gender))
      return false;
    if (givenName == null) {
      if (other.givenName != null)
        return false;
    } else if (!givenName.equals(other.givenName))
      return false;
    if (!Arrays.equals(groups, other.groups))
      return false;
    if (locale == null) {
      if (other.locale != null)
        return false;
    } else if (!locale.equals(other.locale))
      return false;
    if (middleName == null) {
      if (other.middleName != null)
        return false;
    } else if (!middleName.equals(other.middleName))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (nickname == null) {
      if (other.nickname != null)
        return false;
    } else if (!nickname.equals(other.nickname))
      return false;
    if (organisationName == null) {
      if (other.organisationName != null)
        return false;
    } else if (!organisationName.equals(other.organisationName))
      return false;
    if (phoneNumber == null) {
      if (other.phoneNumber != null)
        return false;
    } else if (!phoneNumber.equals(other.phoneNumber))
      return false;
    if (phoneNumberVerified == null) {
      if (other.phoneNumberVerified != null)
        return false;
    } else if (!phoneNumberVerified.equals(other.phoneNumberVerified))
      return false;
    if (picture == null) {
      if (other.picture != null)
        return false;
    } else if (!picture.equals(other.picture))
      return false;
    if (preferredUsername == null) {
      if (other.preferredUsername != null)
        return false;
    } else if (!preferredUsername.equals(other.preferredUsername))
      return false;
    if (profile == null) {
      if (other.profile != null)
        return false;
    } else if (!profile.equals(other.profile))
      return false;
    if (sub == null) {
      if (other.sub != null)
        return false;
    } else if (!sub.equals(other.sub))
      return false;
    if (updatedAt == null) {
      if (other.updatedAt != null)
        return false;
    } else if (!updatedAt.equals(other.updatedAt))
      return false;
    if (website == null) {
      if (other.website != null)
        return false;
    } else if (!website.equals(other.website))
      return false;
    if (zoneinfo == null) {
      if (other.zoneinfo != null)
        return false;
    } else if (!zoneinfo.equals(other.zoneinfo))
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
