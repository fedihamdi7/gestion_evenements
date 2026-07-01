# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ReservationEvenement-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8084 (as configured in application.properties)
EXPOSE 8084

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
