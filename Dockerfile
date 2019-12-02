FROM maven:3-jdk-11 as build

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

FROM openjdk:11-jre-slim

WORKDIR /usr/app
COPY --from=build /usr/src/app/target/marketplace-*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT [ "bash", "-c", "java -Dspring.profiles.active=$SPRING_ACTIVE_PROFILE -jar app.jar" ]
