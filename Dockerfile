FROM openjdk:8-jdk-alpine

MAINTAINER yoann.moranville@gmail.com

#To fix the final name of the jar in the pom.xml later on
COPY ./target/marketplace.jar /opt/marketplace/marketplace.jar
WORKDIR /opt/marketplace/
EXPOSE 8080
CMD ["java", "-jar", "marketplace.jar"]