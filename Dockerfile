FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy project
COPY . .

# Go into backend folder
WORKDIR /app/backend

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build backend
RUN mvn clean install -DskipTests

# Expose correct port
EXPOSE 8080

# Run backend
CMD ["java", "-jar", "target/*.jar"]