# ARGUS OIDC client 

Argus OIDC client is a daemon that has the responsibility of checking validity
and signatures of OAuth tokens for registered trusted OAuth authorization
servers.

The daemon exposes an OAuth token introspection endpoint compliant with [RFC
7662][rfc7662] that can be used by authenticated clients to inspect tokens. The
daemon can only introspect JWT access tokens that contain the `iss` claim.

## How it works

The OIDC client is registered as a client at one (or more) trusted OAuth
authorization servers, and is used by client applications as a gateway for
token validation and introspection.

OIDC performs local JWT validation checks and leverage the introspection
endpoints at trusted AS to inspect a submitted token. The result of a token
introspection is cached, if oidc-client caching is enabled.

## Configuration

Argus OIDC client listens by default on port 8156 and binds on 127.0.0.1. With
this configuration the oidc client will answer token introspection at this
plain http endpoint:

```
http://127.0.0.1/argus-oidc-client/introspect
```

To change the port and address, use the `CLIENT_PORT` and `CLIENT_ADDRESS` env
variables. 

By default oidc client requires client authentication. The default credentials
that client should provide when introspecting a token are:

- username: 'user'
- password: 'password'

These defaults can be changed by setting the `CLIENT_USER_NAME` and
`CLIENT_USER_PASSWORD` env variables. Basic authentication can be disabled by
setting the `CLIENT_ENABLE_BASIC_AUTH` env variables to `false`.

The oidc client should be deployed behind a reverse proxy used to terminate
TLS. When deploying behind a reverse proxy, set the
`CLIENT_USE_FORWARDED_HEADERS` env variable to true.

#### Authorization server configuration

Trusted authorization servers can be configured via a provided application.yml
file with the following structure:

```yaml
oidc:
  clients:
      - issuer-url: https://iam.example
        client-id: iam.example.client-id
        client-secret: iam.example.client-secret
      - issuer-url: https://iam2.example
        client-id: iam2.example.client-id
        client-secret: iam2.example.client-secret

```

See instructions below on how this file can be provided when running the
service with Docker.

### Configuration reference

```bash
# oidc client will bind on this port
CLIENT_PORT=8156 

# oidc client will bind on this address
CLIENT_ADDRESS=127.0.0.1

# Set this to true when deploying behind a reverse proxy (nginx)
CLIENT_USE_FORWARDED_HEADERS=false

# X.509 trust anchors location
X509_TRUST_ANCHORS_DIR=/etc/grid-security/certificates/

# X.509 trust anchors refresh interval (in msec)
X509_TRUST_ANCHORS_REFRESH=14400

# Enable basic authentication 
CLIENT_ENABLE_BASIC_AUTH=true 

# User name credential requested from clients introspecting tokens
CLIENT_USER_NAME=user

# Password  credential requested from clients introspecting tokens
CLIENT_USER_PASSWORD=password 
``` 

## Running the service

### Docker 

1. Define the endpoints and credentials for trusted authorization servers in
   an application.yml file as explained above

2. Define an env file containing the configuration for the oidc-client
   instance following the instructions above, where you also set the running profile for the client as follows:
  ```bash
  OIDC_CLIENT_JAVA_OPTS=-Dspring.profiles.active=prod
  ```
3. Run the service with a command like this:
```console
docker run --env-file=oidc-client.env -v application.yml:/argus-oidc-client/config/application.yml:ro argus-oidc-client:latest
```

[rfc7662]: #tbd
