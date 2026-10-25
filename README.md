# CloudAlpha standard pipeline demo

A small Java 17 / Spring Boot order service built to exercise **every stage of the company standard pipeline**. Use it to demo pipeline generation with the workspace "Pipeline standard" setting.

## The API

| Method | Path | What it does |
|---|---|---|
| `GET` | `/api/orders` | List orders |
| `GET` | `/api/orders/{id}` | One order (404 if unknown) |
| `POST` | `/api/orders` | Create: `{"item":"Laptop","quantity":2,"unitPrice":450.00}` → 201. Orders of 1000 or more get 10% off |
| `POST` | `/api/orders/{id}/confirm` | NEW → CONFIRMED (409 otherwise) |
| `POST` | `/api/orders/{id}/cancel` | NEW → CANCELLED (409 otherwise) |
| `GET` | `/actuator/health` | Health (used by the smoke test and the Kubernetes probes) |

Orders are kept in memory. The point of the demo is the pipeline, not persistence.

## What the repository provides for each standard stage

| # | Stage | In this repository | Command |
|---|---|---|---|
| 1 | Checkout | — | |
| 2 | Source code analysis | Java sources for SonarQube, `.gitleaks.toml` for GitLeaks, `pom.xml` for the Trivy dependency scan | `sonar-scanner`, `gitleaks detect`, `trivy fs .` |
| 3 | Failure gate 1 | — (the pipeline enforces it) | |
| 4 | Compile | `pom.xml` (Maven, Java 17) | `mvn -B compile` |
| 5 | Unit test | `src/test/java/.../order/*Test.java` (JUnit 5), JaCoCo with a **70% line coverage** gate | `mvn -B verify` |
| 6 | Package | `Dockerfile` (multi-stage, pinned base, non-root) | `docker build -t app:$VERSION .` |
| 7 | Push to artifact registry | the image from stage 6 | `docker push <registry>/cloudalpha-standard-demo:$VERSION` |
| 8 | Deploy to Dev | `k8s/deployment.yaml`, `k8s/service.yaml` (probes, limits, non-root, read-only filesystem) | `kubectl -n app-dev-$BUILD apply -f k8s/` |
| 9 | Smoke test | `/actuator/health` | `curl -fsS $BASE_URL/actuator/health` |
| 10 | Regression test | `src/test/java/.../regression/*IT.java` (REST Assured) | `mvn -B -Pregression verify -Dbase.url=$BASE_URL` |
| 11 | Acceptance test | `src/test/resources/features/orders.feature` (Cucumber) | `mvn -B -Pacceptance verify -Dbase.url=$BASE_URL` |
| 12 | Security test (DAST) | `.zap/rules.tsv` | OWASP ZAP baseline against `$BASE_URL` |
| 13 | Failure gate 2 | — (the pipeline enforces it) | |
| 14 | Performance test | `perf/load-test.jmx` (thresholds: errors < 1%, p95 < 500 ms) | `jmeter -n -t perf/load-test.jmx -JHOST=… -JPORT=… -l perf-results.jtl` |

Stages 1–6 are what the platform generates today (Sprint 11). Stages 7–14 arrive with Sprints 12–14; this repository already has what they need.

## Run locally

```bash
mvn -B verify                      # compile, unit tests, coverage gate
mvn spring-boot:run                # http://localhost:8080
mvn -B -Pregression verify         # against http://localhost:8080 (or -Dbase.url=...)
mvn -B -Pacceptance verify
```

## Demo prompt

With **Pipeline standard: Company** selected in the CloudAlpha workspace:

> Generate GitHub Actions for <owner>/cloudalpha-standard-demo: build and test with Maven on Java 17, build the Docker image

No secrets are stored in this repository. Registry and cloud access come from OIDC or the CI secret store.
