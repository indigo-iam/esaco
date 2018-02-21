FROM java:8

ENV OIDC_CLIENT_JAR /argus-oidc-client/oidc-client.jar

RUN mkdir /argus-oidc-client
WORKDIR /argus-oidc-client
COPY oidc-client.jar /argus-oidc-client/oidc-client.jar
CMD java $OIDC_CLIENT_JAVA_OPTS -jar $OIDC_CLIENT_JAR

