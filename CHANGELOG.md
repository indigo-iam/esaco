# Changelog

## 2.0.2 (2026 Sep. 28)

* Bump org.apache.httpcomponents.client5:httpclient5 from 5.6.1 to 5.6.4
* Bump org.springframework.boot:spring-boot-starter-parent from 4.0.6 to 4.1.0
* Bump Spring Boot to 4.1.1
* Move to Java v25
* Bump actions/checkout from v6 to v7
* Bump actions/cache from v6 to v7
* Bump actions/setup-java from v5 to v6
* Bump jib-maven-plugin from 3.1.4 to 3.5.2
* Bump jacoco-plugin from 0.8.12 to 0.8.15
* Bump license-maven-plugin from 3.0 to 5.1.2
* Bump voms-api-java from 3.3.8 to 3.4.0
* Add sonar maven plugin

## 2.0.1 (2026 May 25)

* Bump com.nimbusds:oauth2-oidc-sdk from 10.16 to 11.37.2
* Bump com.jayway.jsonpath:json-path from 2.10.0 to 3.0.0
* Bump com.github.ben-manes.caffeine:caffeine from 3.2.3 to 3.2.4

## 2.0.0 (2026 Mar. 16)

* Remove tokeninfo endpoint
* Upgrade to Spring Boot 4
* Minor updates to other dependencies
* Follow RFC 6749 for OAuth error responses

## 1.0.1 (2025 Nov. 11)

### What's Changed

* Retrieve introspect endpoint from well-known response
* Move to Spring Boot v3.5.7

## 1.0.0 (2025 Nov. 6)

### What's Changed

* Use Docker images based on JDK v21 by @enricovianello in https://github.com/indigo-iam/esaco/pull/37
* Move to Java 21 and other dependencies upgrade by @enricovianello in https://github.com/indigo-iam/esaco/pull/37
* Add testsuite flow by @federicaagostini in https://github.com/indigo-iam/esaco/pull/37
* Update Spring Boot and migrate away from MITREId by @rmiccoli in https://github.com/indigo-iam/esaco/pull/37

**IMPORTANT**: The release v1.0.0 in not backward compatible because of a change: the boolean environment variable `ESACO_USE_FORWARD_HEADERS` has been replaced by `ESACO_FORWARD_HEADERS_STRATEGY` which value is by default `none` or it can be set to `native` when deploying behind a reverse proxy (NGINX)

```
# If you were using ESACO_USE_FORWARD_HEADERS = true, it becomes:
ESACO_FORWARD_HEADERS_STRATEGY = native
# If you were relying on the default value ESACO_USE_FORWARD_HEADERS = false, it automatically becomes:
ESACO_FORWARD_HEADERS_STRATEGY = none
```

## 0.0.6 (2021 Jan. 23)

### What's Changed

* ESACO docker image now is built with [jib][jib] on Github actions. This means that the `JAVA_TOOL_OPTIONS` env variable must be used to set JVM options for the esaco application
* ESACO now runs on Java 11, with Spring boot updated to version 2.2.11
* ESACO now forwards the result of the target introspection endpoint, without introducing any intermediate parsing
* ESACO now requires basic authentication

[jib]: https://github.com/GoogleContainerTools/jib
