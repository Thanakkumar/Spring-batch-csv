FROM openjdk:17-oracle
ADD src/main/resources/application.properties application.properties
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
