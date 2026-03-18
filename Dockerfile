FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /esaco/app

COPY pom.xml .
COPY esaco-app esaco-app

RUN mvn -B -DskipTests package

RUN mkdir -p esaco-app/target/dependency && (cd esaco-app/target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21
ENV ESACO_JAVA_OPTS="-Dspring.profiles.active=prod"
ARG DEPENDENCY=/esaco/app/esaco-app/target/dependency

RUN mkdir /esaco
WORKDIR /esaco

COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib ./lib
COPY --from=builder ${DEPENDENCY}/META-INF ./META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes ./

RUN mkdir -p /etc/grid-security/certificates

CMD ["/bin/sh", "-c", "java ${ESACO_JAVA_OPTS} -cp ./:./lib/* it.infn.mw.esaco.EsacoApplication"]