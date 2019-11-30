FROM openjdk:8-jdk-alpine
ARG JAR_FILE
VOLUME /tmp
ADD target/${JAR_FILE} app.jar
EXPOSE 8080 60000
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]