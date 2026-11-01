## ---- Build stage ---- (Commented since our CI pipelines, already builds the image and hands
# off the CD for idempotency)
#FROM maven:3.9.9-eclipse-temurin-8 AS build
#
#WORKDIR /app
#
#COPY pom.xml .
#COPY src ./src
#
#RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:8-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar"]