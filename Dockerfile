FROM amazoncorretto:11
COPY target/*.jar server-app.jar
ENTRYPOINT ["java", "-jar", "/server-app.jar"]