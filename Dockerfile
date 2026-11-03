# Stage 6, package: a small, non-root image with a pinned base.
FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Alpine runtime: far fewer OS packages, so the image scan at failure gate 1
# has no HIGH/CRITICAL OS findings (the Ubuntu-based jre image had some).
FROM eclipse-temurin:17.0.20_8-jre-alpine-3.24
WORKDIR /app
COPY --from=build /src/target/app.jar app.jar
ENV PORT=8080
EXPOSE 8080
# Pick up Alpine security fixes released after the base image (brotli, expat).
RUN apk upgrade --no-cache && adduser -S -D -u 10001 appuser
USER 10001
# Health is checked by Kubernetes (k8s/deployment.yaml probes) and the smoke test stage.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
