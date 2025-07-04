package it.infn.mw.esaco.test.utils;

import it.infn.mw.esaco.model.IamIntrospection;
import it.infn.mw.esaco.model.IamUser;

public class EsacoTestUtils {

  protected final String CLIENT_ID = "password-grant";
  protected final String CLIENT_SECRET = "secret";
  protected final String USERNAME = "admin";
  protected final String PASSWORD = "password";
  protected final String TOKEN_TYPE = "Bearer";
  protected final String ALG = "RS256";
  protected final String SUB = "73f16d93-2441-4a50-88ff-85360d78c6b5";
  protected final String ISS = "http://localhost:8080/";
  protected final String UNSUPPORTED_ISSUER = "https://iam.local.io/";

  protected final String VALID_JWT =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3M2YxNmQ5My0yNDQxLTRhNTAtODhmZi04NTM2MGQ3OGM2YjUiLCJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvIiwiZXhwIjoxNTA0NjE5MDU5LCJpYXQiOjE1MDQ2MTU0NTksImp0aSI6ImE2N2ZjN2JmLWEzMjMtNDcwMS1iMWVmLThkMjMyMTQ3MjY0NiJ9.Ly4VhjjIXLfQauWybElv8uTQPqHc5M13QQgH9ZDXR0vcG5YPC4J8dzGdlkCCswmKMIdnlJPLR6Mljf20z2aIBaxXw6hsEp7niE4yH-PgqH8GQQdtmXydV1uzJRdxOsOaYDhvBn7QvlGkmC6vtP8maYjBs0delYvst3HrtEkvx7E";
  protected final String EXPIRED_JWT =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3M2YxNmQ5My0yNDQxLTRhNTAtODhmZi04NTM2MGQ3OGM2YjUiLCJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvIiwiZXhwIjoxNTA0NTM0MTQzLCJpYXQiOjE1MDQ1MzA1NDMsImp0aSI6ImYzNzlhZGE5LWQ5MmItNGEzZi1iZjBjLWVhN2U5OWMxNDdhZSJ9.klz4Gi8IDwrYQ9aLQxpLxo5egMZEf_WrQVJudYGnMV5ytmvIbGMV8ya9dyVm074B_zzDBYQPkX5zDjl-sfHQJk4wmY3lerZUCDWOyLvM99TqdKgaV7AAskJv_IOEVkkRDGld2Zc2JLPzbK7_aHR_FzgTrEGZ6dksKN1UYyHbE88";
  protected final String CLIENT_CRED_JWT =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjbGllbnQtY3JlZCIsImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDgwXC8iLCJleHAiOjE1MDQ2MTk4NDIsImlhdCI6MTUwNDYxNjI0MiwianRpIjoiNzg4YjViMzQtMzIyNS00ZGQ4LTk2ZWItNThjNTBkMTdkNzVhIn0.TQM0KCZyNQPpj3XbEuPk133lQR3Caf1Q4xJh2Joerzcxfl0fmQTNZmiZ5hdCuHYJJ3uMyZm230OF7g62LJIKraWmNgZ8fMveSlTzClFeTudoA-3eu6QiiPjWWMW2LJ6TmXJXzF1I_fjM2o4LsNoVvG7nutl_WnsxTvoVewHRqOs";
  protected final String TOKEN_WITH_PARSING_ERR =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3M2YxNmQ5My0yNDQxLTRhNTAtODhmZi04NTM2MGQ3OGM2YjUiLCJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvIiwiZXhwIjoxNTA0NjI2NDkwLCJpYXQiOjE1MDQ2MjI4OTAsImp0aSI6IjVmYThmYjgwLTYyOGMtNGY5OC1iZjI4LTc1NjAxYWM3NjY1MSJ9.clGms5RrhTkqvj_Ccb9EBLqmInuEjEiJW1YLAWBONsO5z0pWTyixWy6AFMup98mQKHEfiGI8IzO0sxU0yAJjxkJR-sTIurz1gxg9rhndd0xxCHdNQF0vc-Y3BRzn2pLmYMb8gXmINYCCMhcV1WTxJMMkktDQg_5CF3x_9ZOeIgU";
  protected final String TOKEN_WITH_CONNECTION_ERR =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3M2YxNmQ5My0yNDQxLTRhNTAtODhmZi04NTM2MGQ3OGM2YjUiLCJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvIiwiZXhwIjoxNTA0NjI3MjI1LCJpYXQiOjE1MDQ2MjM2MjUsImp0aSI6IjNmNjljYmE3LWI3NjQtNGIwNi05MTk3LTA1YTk1YTFmMDg2MiJ9.AsKhMYM1zufCr9yY79tzBnh6AlCU2GBDzF5jzSC0kWjFDTW_0Bkc4GeBk4gUnuEJmt63KEs7d_-glBNHZArcBECosJU9dMJ7UW_5FXRxLzPRRqNA0SKi31KkAQczhw_JZiEscMBDNT31__IHW5IX59Du_QD6PoDoni-Iac1-_-M";
  protected final String TOKEN_FROM_UNKNOWN_ISSUER =
      "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3M2YxNmQ5My0yNDQxLTRhNTAtODhmZi04NTM2MGQ3OGM2YjUiLCJpc3MiOiJodHRwczpcL1wvaWFtLmxvY2FsLmlvXC8iLCJleHAiOjE1MTA1Nzg1NDksImlhdCI6MTUxMDU3NDk0OSwianRpIjoiMWNiMDYxMWItNGZlYS00NjRhLWJiNDktMzllNzE1MzFkMmJjIn0.cFXl8zJQUqLEF2kuaxx_w4znm4rMGlGguN_01cs5CSKV6FMPCVGLzDeDXg068uQWtExINBOzdtRlzOgFR5-s-9XFlaQBBmsjsuZBMwPlQvh-ceQGmAuEZt-QmU-kh7zarAsa2N4wkExFdP7iB6Mz8RFjBN3OPA5puMOpDumCQSQ";
  protected final String EXTRA_INFORMATION_JWT =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY3RpdmUiOnRydWUsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUiLCJleHBpcmVzX2F0IjoiMjAxNy0wOS0wNFQxNjowOTowMyswMjAwIiwiZXhwIjoxNTg0NjMxNjcwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoiNzNmMTZkOTMtMjQ0MS00YTUwLTg4ZmYtODUzNjBkNzhjNmI1IiwidW5lY2Vzc2FyeV9maWVsZCI6InVuZWNlc3NhcnlfaW5mb3JtYXRpb24iLCJ1c2VyX2lkIjoiYWRtaW4iLCJjbGllbnRfaWQiOiJwYXNzd29yZC1ncmFudCIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJncm91cHMiOlsiUHJvZHVjdGlvbiIsIkFuYWx5c2lzIl0sInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwib3JnYW5pc2F0aW9uX25hbWUiOiJpbmRpZ28tZGMiLCJuYW1lIjoiQWRtaW4gVXNlciIsImVtYWlsIjoiYWRtaW5AZXhhbXBsZS5vcmciLCJncm91cE5hbWVzIjpbIlByb2R1Y3Rpb24iLCJBbmFseXNpcyJdLCJlZHVfcGVyc29uX2VudGl0bGVtZW50cyI6WyJ1cm46bWFjZTplZ2kuZXU6Z3JvdXA6dm8udGVzdC5lZ2kuZXU6cm9sZT1tZW1iZXIjYWFpLmVnaS5ldSJdLCJlZHVwZXJzb25fZW50aXRsZW1lbnQiOlsidXJuOm1hY2U6ZWdpLmV1Omdyb3VwOnZvLnRlc3QuZWdpLmV1OnJvbGU9bWVtYmVyI2FhaS5lZ2kuZXUiXSwiYWNyIjoiaHR0cHM6Ly9hYWkuZWdpLmV1L0xvQSNTdWJzdGFudGlhbCIsImp0aSI6IjkyYzQ3ODI1LWJjYTAtNDBlNy05NjllLWE2NDQ3ZmZiNmFjOCIsImlhdCI6MTU4NDYyODA3MH0.oSer92_vf-_OlSezwyOa-bfOTObS9_ph-_M3_yD-gEk";

  protected final String EXTRA_INFORMATION_INTROSPECTION =
      "{\"active\":true,\"scope\":\"openid profile\",\"expires_at\":\"2017-09-04T16:09:03+0200\",\"exp\":1504534143,\"iss\":\"http://localhost:8080/\",\"sub\":\"73f16d93-2441-4a50-88ff-85360d78c6b5\",\"unecessary_field\":\"unecessary_information\",\"user_id\":\"admin\",\"client_id\":\"password-grant\",\"token_type\":\"Bearer\",\"groups\":[\"Production\",\"Analysis\"],\"preferred_username\":\"admin\",\"organisation_name\":\"indigo-dc\",\"name\":\"Admin User\",\"email\":\"admin@example.org\",\"groupNames\":[\"Production\",\"Analysis\"],\"edu_person_entitlements\":[\"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu\"],\"eduperson_entitlement\":[\"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu\"],\"acr\":\"https://aai.egi.eu/LoA#Substantial\"}";

  protected final IamIntrospection VALID_INTROSPECTION =
      new IamIntrospection.Builder(true).addField("scope", new String[] {"openid", "profile"})
        .addField("expires_at", "2017-09-04T16:09:03+0200")
        .addField("exp", 1504534143L)
        .addField("iss", ISS)
        .addField("sub", "73f16d93-2441-4a50-88ff-85360d78c6b5")
        .addField("user_id", "admin")
        .addField("client_id", "password-grant")
        .addField("token_type", "Bearer")
        .addField("groups", new String[] {"Production", "Analysis"})
        .addField("preferred_username", "admin")
        .addField("organisation_name", "indigo-dc")
        .addField("name", "Admin User")
        .addField("email", "admin@example.org")
        .addField("entitlements",
            new String[] {"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu"})
        .addField("eduperson_entitlement",
            new String[] {"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu"})
        .addField("acr", "https://aai.egi.eu/LoA#Substantial")
        .build();

  protected final IamUser VALID_USERINFO =
      new IamUser.Builder("73f16d93-2441-4a50-88ff-85360d78c6b5").addField("name", "Admin User")
        .addField("preferred_username", "admin")
        .addField("given_name", "Admin")
        .addField("family_name", "User")
        .addField("email", "admin@example.org")
        .addField("gender", "M")
        .addField("updated_at", "Mon Sep 04 15:08:36 CEST 2017")
        .addField("groups", new String[] {"Production", "Analysis"})
        .addField("organisation_name", "indigo-dc")
        .addField("entitlements",
            new String[] {"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu"})
        .addField("eduperson_entitlement",
            new String[] {"urn:mace:egi.eu:group:vo.test.egi.eu:role=member#aai.egi.eu"})
        .addField("acr", "https://aai.egi.eu/LoA#Substantial")
        .build();


  protected final IamIntrospection EXPIRED_INTROSPECTION =
      new IamIntrospection.Builder(false).build();

  protected final IamIntrospection CLIENT_CRED_INTROSPECTION = new IamIntrospection.Builder(true)
    .addField("scope", new String[] {"read-tasks", "write-tasks"})
    .addField("expires_at", "2017-09-05T15:57:22+0200")
    .addField("exp", 1504619842L)
    .addField("user_id", "client-cred")
    .addField("client_id", "client-cred")
    .addField("token_type", "Bearer")
    .build();

  protected final String ISSUER_CONFIGURATION =
      "{\"request_parameter_supported\":true,\"claims_parameter_supported\":false,\"introspection_endpoint\":\"http://localhost:8080/introspect\",\"scopes_supported\":[\"openid\",\"profile\",\"email\",\"address\",\"phone\",\"offline_access\"],\"issuer\":\"http://localhost:8080/\",\"userinfo_encryption_enc_values_supported\":[\"A256CBC+HS512\",\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\",\"A128CBC+HS256\"],\"id_token_encryption_enc_values_supported\":[\"A256CBC+HS512\",\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\",\"A128CBC+HS256\"],\"authorization_endpoint\":\"http://localhost:8080/authorize\",\"service_documentation\":\"http://localhost:8080/about\",\"request_object_encryption_enc_values_supported\":[\"A256CBC+HS512\",\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\",\"A128CBC+HS256\"],\"userinfo_signing_alg_values_supported\":[\"HS256\",\"HS384\",\"HS512\",\"RS256\",\"RS384\",\"RS512\",\"ES256\",\"ES384\",\"ES512\",\"PS256\",\"PS384\",\"PS512\"],\"claims_supported\":[\"sub\",\"name\",\"preferred_username\",\"given_name\",\"family_name\",\"middle_name\",\"nickname\",\"profile\",\"picture\",\"website\",\"gender\",\"zoneinfo\",\"locale\",\"updated_at\",\"birthdate\",\"email\",\"email_verified\",\"phone_number\",\"phone_number_verified\",\"address\",\"organisation_name\",\"groups\",\"external_authn\"],\"claim_types_supported\":[\"normal\"],\"op_policy_uri\":\"http://localhost:8080/about\",\"token_endpoint_auth_methods_supported\":[\"client_secret_post\",\"client_secret_basic\",\"none\"],\"token_endpoint\":\"http://localhost:8080/token\",\"response_types_supported\":[\"code\",\"token\"],\"request_uri_parameter_supported\":false,\"userinfo_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"],\"grant_types_supported\":[\"authorization_code\",\"implicit\",\"refresh_token\",\"client_credentials\",\"password\",\"urn:ietf:params:oauth:grant-type:jwt-bearer\",\"urn:ietf:params:oauth:grant_type:redelegate\",\"urn:ietf:params:oauth:grant-type:token-exchange\"],\"revocation_endpoint\":\"http://localhost:8080/revoke\",\"userinfo_endpoint\":\"http://localhost:8080/userinfo\",\"token_endpoint_auth_signing_alg_values_supported\":[\"HS256\",\"HS384\",\"HS512\",\"RS256\",\"RS384\",\"RS512\",\"ES256\",\"ES384\",\"ES512\",\"PS256\",\"PS384\",\"PS512\"],\"op_tos_uri\":\"http://localhost:8080/about\",\"require_request_uri_registration\":false,\"code_challenge_methods_supported\":[\"plain\",\"S256\"],\"id_token_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"],\"jwks_uri\":\"http://localhost:8080/jwk\",\"subject_types_supported\":[\"public\",\"pairwise\"],\"id_token_signing_alg_values_supported\":[\"HS256\",\"HS384\",\"HS512\",\"RS256\",\"RS384\",\"RS512\",\"ES256\",\"ES384\",\"ES512\",\"PS256\",\"PS384\",\"PS512\",\"none\"],\"registration_endpoint\":\"http://localhost:8080/register\",\"request_object_signing_alg_values_supported\":[\"HS256\",\"HS384\",\"HS512\",\"RS256\",\"RS384\",\"RS512\",\"ES256\",\"ES384\",\"ES512\",\"PS256\",\"PS384\",\"PS512\"],\"request_object_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"]}";
}
