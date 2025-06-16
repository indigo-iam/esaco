# ESACO integration example

This folder contains a `docker-compose` file to deploy the
necessary services that allow to showcase the integration between
the Apache module [mod-auth-openidc][mod-auth-openidc] and ESACO.

## Deployment

Build the trustanchor with

```
$ docker compose build --no-cache trust
```

and run the compose with

```
$ docker compose up -d
```

which allows to start the following services

* _trust_: volume containing the Test CA certificate, shared with other services. The container also populates a `/certs` volume which includes an X.509 server/key certificate
* _iam-be1_: token issuer hosted at [https://iam1.test.example][iam1]
* _iam-be2_: token issuer hosted at [https://iam2.test.example][iam2]
* _esaco_: is the ESACO daemon, registered as a client into `iam-be1` and `iam-be2`
* _apache_: is the Apache daemon hosted at [https://apache.test.example][apache]. It allows access to different resources via IAM tokens. It is not an IAM client: the token validation process is delegated to the `esaco` service
* _nginx_: used as a reverse proxy for `iam-be1`, `iam-be2`, `esaco` and `apache` services.

__Notes:__
* To resolve the hostname of the services running on localhost please add the following line to your `/etc/hosts` file

  ```
  127.0.0.1	esaco.test.example apache.test.example iam1.test.example iam2.test.example
  ```
* The services use a test certificate (the DN contains `*.test.example`) issued by a Test CA. To avoid warnings from the browser add it among the Authorities: copy the Test CA certificate from the container to the current directory with

  ```
  $ docker-compose cp trust:/etc/grid-security/certificates/igi-test-ca.pem .
  ```
* To allow SSL handshake in the next `curl` commands (now ignored with the `-k` option), use the specified Test CA certificate file with `--cacert igi-test-ca.pem`.

## Apache

Apache exposes three endpoints:

* `/iam1`: to access this resource you need to authenticate with a Bearer Token issued by `iam-be1`. Obtain and save the Bearer Token with

  ```
  $ BT=$(curl -u client-cred:secret https://iam1.test.example/token -d grant_type=client_credentials -k -s | jq .access_token | tr -d '"')
  ```

  To access the resource with Bearer Authentication, type

  ```
  $ curl -H "Authorization: Bearer $BT" https://apache.test.example/iam1 -k -L
  Hello, this is /iam1
  ```

* `/iam2`: to access this resource you need to authenticate with a Bearer Token issued by `iam-be2`. Obtain and save the Bearer Token with

  ```
  $ BT=$(curl -u client-cred:secret https://iam2.test.example/token -d grant_type=client_credentials -k -s | jq .access_token | tr -d '"')
  ```

  To access the resource with Bearer Authentication, type

  ```
  $ curl -H "Authorization: Bearer $BT" https://apache.test.example/iam2 -k -L
  Hello, this is /iam2
  ```

* `/web`: this is a DEMO web page whose access triggers an OIDC login with `iam-be1`. A client on this IAM instance have to be registered beforehand. The necessary parameters of the client configuration are

  * _client ID_: demo_client
  * _redirect URI_: https://apache.test.example/web/redirect_uri
  * _client secret_: secret

  This step has to be done with any IAM restart. Then, access to this resource is via [https://apace.test.example/web][apache-web].

## ESACO

ESACO is registered as client both in `iam-be1` and `iam-be2`. Those clients are also allowed to call the IAM introspection endpoints. In particular, both IAMs are started in developed mode, where a list of pre-configured clients are available. The ESACO clients are set up in the `application.yml` configuration file, and use the `password-grant` client ID.

The Apache server is configured to perform a remote token validation through ESACO.

An HTTP request to the ESACO daemon in order to check the validity of a token `BT` looks like

```
$ curl -u user:password https://esaco.test.example/introspect -d token=$BT -k -s | jq
{
  "active": true,
  "scope": "openid profile offline_access storage.read:/ wlcg.groups",
  "expires_at": "2023-06-01T18:19:28+0200",
  "exp": 1685636368,
  "sub": "client-cred",
  "client_id": "client-cred",
  "token_type": "Bearer",
  "iss": "https://iam2.test.example/"
}
```

[mod-auth-openidc]: https://github.com/OpenIDC/mod_auth_openidc
[iam1]: https://iam1.test.example
[iam2]: https://iam2.test.example
[apache]: https://apache.test.example
[apache-web]: https://apace.test.example/web