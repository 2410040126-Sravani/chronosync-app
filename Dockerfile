FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build project
RUN mvn clean install -DskipTests

EXPOSE 8082

CMD ["java", "-jar", "target/*.jar"]