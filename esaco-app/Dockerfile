FROM eclipse-temurin:17

RUN mkdir /esaco; mkdir -p /etc/grid-security/certificates
WORKDIR /esaco
COPY target/esaco-app-*.jar /esaco/esaco-app.jar

CMD ["java", "-jar", "esaco-app.jar"]
