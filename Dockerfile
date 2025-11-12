FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /esaco/app
RUN apk add maven
COPY .git .git

COPY pom.xml .
COPY esaco-app esaco-app

RUN mvn package -Dmaven.test.skip

RUN mkdir -p esaco-app/target/dependency && (cd esaco-app/target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21
ENV ESACO_JAVA_OPTS="-Dspring.profiles.active=prod"
ARG DEPENDENCY=/esaco/app/esaco-app/target/dependency

RUN mkdir /esaco
WORKDIR /esaco

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib ./lib
COPY --from=build ${DEPENDENCY}/META-INF ./META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes ./

RUN mkdir -p /etc/grid-security/certificates

SHELL ["/bin/bash", "-c"]
ENTRYPOINT java ${ESACO_JAVA_OPTS} -cp ./:./lib/* it.infn.mw.esaco.EsacoApplication