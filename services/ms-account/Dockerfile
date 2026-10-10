FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/ms-account/pom.xml .
COPY services/ms-account/src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/ms-account-*.jar app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
