# Construire le projet avec Maven et Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Compiler le projet et executer les tests
RUN mvn -B clean package

# Image pour executer Java
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/classes ./classes

ENTRYPOINT ["java", "-cp", "/app/classes", "org.daar.Main"]
