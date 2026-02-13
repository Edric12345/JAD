# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.war app.war 
# Note: Use .war if you are deploying JSPs, or .jar if it's a standard Spring Boot app.
# Looking at your structure, Spring Boot usually builds a .jar by default.
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.war"]
