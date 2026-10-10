FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/api-gateway/pom.xml .
COPY services/api-gateway/src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/api-gateway-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
