# syntax=docker/dockerfile:1

# ---- Build stage ----
# Compiles the requested module (plus its reactor dependencies) and keeps only the
# executable Spring Boot fat jar. One image serves all four apps; docker-compose
# selects the module with the MODULE build arg.
FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG MODULE=eventmind-command
WORKDIR /build
COPY . .
RUN mvn -q -pl ${MODULE} -am package -DskipTests \
    && cp ${MODULE}/target/${MODULE}-*.jar /app.jar

# ---- Runtime stage ----
# Plain JRE: one app per container. Config comes from environment variables
# injected by compose / the orchestrator; the fat jar never depends on a .env file.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar
EXPOSE 8080 8081 8082 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
