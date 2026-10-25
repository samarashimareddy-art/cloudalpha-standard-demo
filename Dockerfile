# Stage 6, package: a small, non-root image with a pinned base.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17.0.12_7-jre
WORKDIR /app
COPY --from=build /src/target/app.jar app.jar
ENV PORT=8080
EXPOSE 8080
RUN useradd --system --create-home --uid 10001 appuser
USER 10001
# Health is checked by Kubernetes (k8s/deployment.yaml probes) and the smoke test stage.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
