# Metrics and Health (Observability)

This document describes **MVP #4: Metrics & health endpoints** — what is exposed and how to use it.

---

## Endpoints

| Endpoint | Auth | Purpose |
|----------|------|---------|
| `GET /actuator/health` | **Public** | Liveness/readiness for load balancers and Kubernetes probes. |
| `GET /actuator/health/**` | **Public** | Component health (e.g. DB). Details shown when authorized. |
| `GET /actuator/metrics` | **ADMIN** | List of metric names. |
| `GET /actuator/metrics/{name}` | **ADMIN** | Single metric (e.g. `feature.flag.evaluations`). |
| `GET /actuator/prometheus` | **ADMIN** | All metrics in Prometheus scrape format. |

Health is public so orchestration (e.g. k8s, ECS) can probe without credentials. Metrics and Prometheus are admin-only so they are not exposed to anonymous clients.

---

## Custom metrics

The application records:

### 1. `feature.flag.evaluations` (Counter)

Incremented on every flag evaluation. Tags:

- **feature_key** — the flag key (e.g. `new-checkout`).
- **environment** — `DEV`, `STAGING`, or `PROD`.
- **result** — `on` or `off`.
- **reason** — e.g. `TARGETED_USER`, `ROLLOUT_100`, `FLAG_DISABLED`, `FLAG_NOT_FOUND`.

Example (Prometheus):  
`feature_flag_evaluations_total{feature_key="new-checkout",environment="PROD",reason="ROLLOUT_100",result="on"} 42`

### 2. `feature.flag.evaluation.duration` (Timer)

Measures evaluation latency. Tags:

- **feature_key**
- **environment**

You can use it for percentiles (e.g. p99) and throughput in Prometheus/Grafana.

---

## Configuration (application.yml)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized   # full details only when authenticated
  metrics:
    tags:
      application: feature-flags      # added to every metric
```

---

## Example: health check

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Example response (up):

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "H2", ... } },
    "diskSpace": { "status": "UP" },
    ...
  }
}
```

---

## Example: list metrics (as admin)

```bash
curl -s -u admin:admin123 http://localhost:8080/actuator/metrics | jq '.names[]' | grep feature
```

Example: get evaluation counter

```bash
curl -s -u admin:admin123 "http://localhost:8080/actuator/metrics/feature.flag.evaluations"
```

---

## Example: Prometheus scrape (as admin)

```bash
curl -s -u admin:admin123 http://localhost:8080/actuator/prometheus | grep feature_flag
```

Prometheus can scrape this URL (with optional basic auth or a dedicated scrape user) and store the metrics for dashboards and alerting.

---

## Files added/updated

| File | Change |
|------|--------|
| `pom.xml` | Added `spring-boot-starter-actuator` and `micrometer-registry-prometheus`. |
| `application.yml` | `management.endpoints.web.exposure.include`, `health.show-details`, `metrics.tags`. |
| `SecurityConfig.java` | `/actuator/health` permitAll; `/actuator/**` hasRole ADMIN. |
| `FeatureEvaluationService.java` | Injects `MeterRegistry`; records counter per evaluation and timer around `doEvaluate`. |
| `FeatureEvaluationServiceTest.java` | Injects `SimpleMeterRegistry` in constructor. |

This gives you production-style observability for CV and interviews.
