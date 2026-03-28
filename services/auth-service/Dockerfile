# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY services/eureka-server/pom.xml services/eureka-server/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/delivery-service/pom.xml services/delivery-service/pom.xml
COPY services/tracking-service/pom.xml services/tracking-service/pom.xml
COPY services/admin-service/pom.xml services/admin-service/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services ./services
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl services/auth-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/auth-service/target/auth-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
