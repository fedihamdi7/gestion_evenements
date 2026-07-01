# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- run ----
# This service starts an EMBEDDED MongoDB (flapdoodle) that needs a few shared libs.
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl libcurl4 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
