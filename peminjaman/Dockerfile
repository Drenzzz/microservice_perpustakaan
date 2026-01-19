# Optimized Dockerfile for Local Dev (Uses pre-built JAR)
# To revert to multi-stage build, uncomment the lines below and remove the COPY target line

# Build Stage (Commented out for speed)
# FROM maven:3.9.6-eclipse-temurin-21 AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Uses locally built JAR from target/ directory
COPY target/*.jar app.jar
# COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
CMD ["java","-jar","app.jar"]
