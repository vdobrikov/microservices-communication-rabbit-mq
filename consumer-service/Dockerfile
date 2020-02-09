FROM openjdk:8-jdk-alpine
ARG SUBSCRIBER_TOPIC
ARG JAR_FILE
ENV SUBSCRIBER_TOPIC $SUBSCRIBER_TOPIC
VOLUME /tmp
ADD target/${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]