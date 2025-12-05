FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE ${SERVER_PORT:-48080}
ENTRYPOINT ["java", "-jar", "app.jar"]

