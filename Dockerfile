FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /esaco/app
RUN apk add maven
COPY .git .git

COPY pom.xml .
COPY esaco-app esaco-app
COPY esaco-common esaco-common

RUN mvn package -Dmaven.test.skip

RUN mkdir -p esaco-app/target/dependency && (cd esaco-app/target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17
ENV ESACO_JAVA_OPTS="-Dspring.profiles.active=prod"
ARG DEPENDENCY=/esaco/app/esaco-app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

RUN mkdir -p /etc/grid-security/certificates

SHELL ["/bin/bash", "-c"]
ENTRYPOINT java ${ESACO_JAVA_OPTS} -cp app:app/lib/* it.infn.mw.esaco.EsacoApplication