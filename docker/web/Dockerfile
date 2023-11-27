FROM openjdk:17
# Add build/libs/app.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=classpath:/application.yml,classpath:/application-real.properties,classpath:/application-real-db.properties", "-Dspring.profiles.active=real", "app.jar"]
