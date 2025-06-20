FROM eclipse-temurin:17

RUN mkdir /esaco; mkdir -p /etc/grid-security/certificates
WORKDIR /esaco
COPY target/esaco-app-*.jar /esaco/esaco-app.jar

ENV ESACO_JAVA_OPTS="-Dspring.profiles.active=prod"

SHELL ["/bin/bash", "-c"]
ENTRYPOINT java -jar $ESACO_JAVA_OPTS esaco-app.jar
