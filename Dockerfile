FROM eclipse-temurin:21-jdk as build
WORKDIR /workspace/app

# Copy maven executable and config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar

# Environment variables
ENV AWS_REGION=us-west-2
ENV PORT=5000

EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]