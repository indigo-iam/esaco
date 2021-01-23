# Changelog

## 0.0.6 (23-01-2021)

- ESACO docker image now is built with [jib][jib] on Github actions. This
  means that the `JAVA_TOOL_OPTIONS` env variable must be used to set JVM
  options for the esaco application. (#21)

- ESACO now runs on Java 11, with Spring boot updated to version 2.2.11 (#20)

- ESACO now forwards the result of the target introspection endpoint, without
  introducing any intermediate parsing (#12)

- ESACO now requires basic authentication
