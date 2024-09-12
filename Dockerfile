FROM openjdk:21-jdk-slim

WORKDIR /crypto_app

COPY pom.xml .

COPY src ./src

RUN apt-get update && apt-get install -y maven

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/crypto_app-0.0.1-SNAPSHOT.jar"]