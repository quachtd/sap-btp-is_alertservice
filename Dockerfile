# syntax=docker/dockerfile:1

# Build stage runs on the host architecture (fast on Apple Silicon).
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage targets the deployment platform (e.g. linux/amd64).
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p /app/certs \
    && chown -R spring:spring /app
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
