FROM maven:3-eclipse-temurin-21 AS build

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

FROM eclipse-temurin:21

WORKDIR /usr/app
COPY --from=build /usr/src/app/target/marketplace-*.jar ./app.jar

ENV APPLICATION_PROFILE='dev'

EXPOSE 8080
ENTRYPOINT [ "java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9990", "-jar", "app.jar" ]
