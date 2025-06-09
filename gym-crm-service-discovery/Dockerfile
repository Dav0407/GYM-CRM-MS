FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /app

# Copy everything and set permissions
COPY . .
RUN chmod +x ./gradlew

# Build the app, skipping tests
RUN ./gradlew clean build -x test --no-daemon

# Use a smaller base image just for running the app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your Spring Boot app uses
EXPOSE 8765

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
