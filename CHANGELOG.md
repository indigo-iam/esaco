# Changelog

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
