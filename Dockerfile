FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/SmartMedTender-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
