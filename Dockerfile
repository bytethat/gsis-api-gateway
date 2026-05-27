# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Maven wrapper configuration and project descriptor
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Pre-fetch dependencies (helps layer caching, but note jaxws-maven-plugin needs network for WSDLs)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build the application
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as a non-root system user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy executable jar from builder stage
COPY --from=builder /app/target/gsis-api-gateway-*.jar app.jar

# Expose server port (configured in application.yml to 8080)
EXPOSE 8080

# Configure JVM flags and launch the application
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
