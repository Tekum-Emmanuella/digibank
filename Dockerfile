FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root user (Principle of Least Privilege)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Wildcard avoids breaking on pom.xml version bumps
COPY --chown=appuser:appgroup digibank-web/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]