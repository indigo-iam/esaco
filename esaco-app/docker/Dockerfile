FROM java:8

ENV ESACO_JAR /esaco/esaco-app.jar

RUN mkdir /esaco; mkdir -p /etc/grid-security/certificates
WORKDIR /esaco
COPY esaco-app.jar /esaco/esaco-app.jar

CMD java $ESACO_JAVA_OPTS -jar $ESACO_JAR

