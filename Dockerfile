FROM maven:3-jdk-11 as build
ARG APPLICATION_PROFILE

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

FROM openjdk:11-jre-slim

WORKDIR /usr/app
COPY --from=build /usr/src/app/target/marketplace-*.jar ./app.jar

ARG APPLICATION_PROFILE
#ENV APPLICATION_PROFILE=''

EXPOSE 8080
ENTRYPOINT [ "java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9990", "-jar", "app.jar" ]
