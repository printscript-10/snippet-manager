FROM gradle:8.5-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src

ARG USERNAME
ARG TOKEN

ENV USERNAME=$USERNAME
ENV TOKEN=$TOKEN

RUN gradle assemble
FROM eclipse-temurin:21-jre

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

EXPOSE 8081

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java","-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "-Dspring.profiles.active=production","/app/spring-boot-application.jar"]
