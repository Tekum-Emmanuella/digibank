FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml dependency-check-suppressions.xml ./
COPY common-module/pom.xml common-module/pom.xml
COPY customer-module/pom.xml customer-module/pom.xml
COPY account-module/pom.xml account-module/pom.xml
COPY transfer-module/pom.xml transfer-module/pom.xml
COPY digibank-web/pom.xml digibank-web/pom.xml

COPY . .
RUN mvn -B -ntp -DskipTests -Ddependency-check.skip=true clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /workspace/digibank-web/target/digibank-web-*.jar app.jar

USER appuser:appgroup
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
