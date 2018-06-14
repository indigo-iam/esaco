# ESACO

ESACO is a daemon that has the responsibility of checking validity
and signatures of OAuth tokens for registered trusted OAuth authorization
servers.

The daemon exposes an OAuth token introspection endpoint compliant with [RFC
7662][rfc7662] that can be used by authenticated clients to inspect tokens. The
daemon can only introspect JWT access tokens that contain the `iss` claim.

## How it works

ESACO is registered as a client at one (or more) trusted OAuth
authorization servers, and is used by client applications as a gateway for
token validation and introspection.

ESACO performs local JWT validation checks and leverages the introspection
endpoints at trusted AS to inspect a submitted token. The result of a token
introspection is cached, if caching is enabled.

## Configuration

ESACO listens by default on port 8156 on all interfaces.

The intropection endpoint answers at `/introspect`:

```
http://esaco.example/introspect
```

To change the port and address, use the `ESACO_BIND_PORT` and
`ESACO_BIND_ADDRESS` environment variables.

By default ESACO requires client authentication. The default credentials
that client should provide when introspecting a token are:

- username: 'user'
- password: 'password'

These defaults can be changed by setting the `ESACO_USER_NAME` and
`ESACO_USER_PASSWORD` environment variables. Basic authentication can be disabled by
setting the `ESACO_ENABLE_BASIC_AUTH` environment variables to `false`.

ESACO should be deployed behind a reverse proxy used to terminate
TLS. When deploying behind a reverse proxy, set the
`ESACO_USE_FORWARD_HEADERS` environment variable to true.

#### Authorization server configuration

Trusted authorization servers can be configured via a provided `application.yml`
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

#### Cache management

ESACO uses an internal in-memory cache to cache results of token
introspection and userinfo calls.

The size and the eviction time for the cache can be set using the
`ESACO_CACHE_SPEC` environment variable.

By default, the cache is setup with a maximum size of 500 elements and the
records are evicted after 60 seconds, as follows:

```bash
ESACO_CACHE_SPEC=maximumSize=500,expireAfterWrite=60s
```
More configuration options can be found into [caffeine official
documentation](https://github.com/ben-manes/caffeine/wiki).

The cache can be disabled by setting the `ESACO_CACHE` environment variable as
follows:

```
ESACO_CACHE=none
```

### Configuration reference

```bash
# ESACO client will bind on this port
ESACO_BIND_PORT=8156

# ESACO client will bind on this address
ESACO_BIND_ADDRESS=0.0.0.0

# Set this to true when deploying behind a reverse proxy (nginx)
ESACO_USE_FORWARD_HEADERS=false

# X.509 trust anchors location
X509_TRUST_ANCHORS_DIR=/etc/grid-security/certificates/

# X.509 trust anchors refresh interval (in msec)
X509_TRUST_ANCHORS_REFRESH=14400

# or use a single-file CA bundle without CRLs
#X509_TRUST_ANCHORS_BUNDLE=/etc/ssl/certs/ca-bundle.crt
#X509_TRUST_ANCHORS_TYPE=bundle

# Enable basic authentication
ESACO_ENABLE_BASIC_AUTH=true

# User name credential requested from clients introspecting tokens
ESACO_USER_NAME=user

# Password  credential requested from clients introspecting tokens
ESACO_USER_PASSWORD=password

# Enables caching of the results of introspection and userinfo calls
# To disable the cache set ESACO_CACHE=none
ESACO_CACHE=caffeine

# The size and eviction time policies for the cache
ESACO_CACHE_SPEC=maximumSize=500,expireAfterWrite=60s

# TLS version
ESACO_TLS_VERSION=TLSv1.2
```

## Running the service

### Docker

1. Define the endpoints and credentials for trusted authorization servers in
   an application.yml file as explained above

2. Define an environment file containing the configuration for ESACO
   instance following the instructions above

3. Run the service with a command like this:
  ```console
  docker run --env-file=esaco.env -v application.yml:/esaco/config/application.yml:ro indigoiam/esaco:latest
  ```

[rfc7662]: https://tools.ietf.org/html/rfc7662
