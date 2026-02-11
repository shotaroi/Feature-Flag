# Feature Flag

A backend feature-flag service: evaluate flags per user and environment, manage flags and API keys via REST, with audit logging, metrics, and OpenAPI docs.

---

## Features

- **Environments** — Separate config per `DEV`, `STAGING`, `PROD` (same flag key, different state).
- **Evaluation** — Check if a flag is on for a user (allowlist + percentage rollout). Requires **X-API-Key**.
- **Admin API** — CRUD flags, add/remove target users, view change history. Requires **HTTP Basic** (admin).
- **API keys** — Create/list/revoke keys for the evaluation endpoint; keys are hashed (never stored in plaintext).
- **Audit log** — Every flag change (create, update, add/remove target) is recorded with who and when.
- **Metrics & health** — Actuator health, custom metrics (evaluations counter, latency timer), Prometheus scrape.
- **OpenAPI** — Swagger UI and `/v3/api-docs` for interactive docs.

---

## Tech stack

- **Java 21**, **Spring Boot 4**
- **Spring Data JPA**, **H2** (in-memory; replace with PostgreSQL for production)
- **Flyway** — schema migrations
- **Spring Security** — Basic auth for admin, API key for evaluation
- **Micrometer / Actuator** — health, metrics, Prometheus
- **Springdoc OpenAPI** — Swagger UI

---

## Prerequisites

- **Java 21**
- **Maven 3.9+**

---

## Quick start

```bash
# Run the application
mvn spring-boot:run
```

Server starts at **http://localhost:8080**.

### Default admin user

- **Username:** `admin`
- **Password:** `admin123`

Use these for HTTP Basic on admin endpoints and for Swagger UI.

---

## Main endpoints

| Purpose | Endpoint | Auth |
|--------|----------|------|
| Evaluate a flag | `GET /api/flags/{featureKey}/evaluate?environment=PROD&userId=...` | **X-API-Key** |
| List flags (admin) | `GET /api/admin/flags?environment=PROD` | HTTP Basic (admin) |
| Create flag (admin) | `POST /api/admin/flags` | HTTP Basic (admin) |
| Create API key (admin) | `POST /api/admin/api-keys` | HTTP Basic (admin) |
| Flag history (admin) | `GET /api/admin/flags/{featureKey}/history?environment=PROD` | HTTP Basic (admin) |
| Health | `GET /actuator/health` | Public |
| Metrics / Prometheus | `GET /actuator/metrics`, `GET /actuator/prometheus` | HTTP Basic (admin) |
| Swagger UI | `GET /swagger-ui.html` | HTTP Basic (admin) |
| H2 console | `GET /h2-console` | Public (dev only) |

---

## Try it

1. **Create an API key** (as admin):

   ```bash
   curl -u admin:admin123 -X POST http://localhost:8080/api/admin/api-keys \
     -H "Content-Type: application/json" \
     -d '{"name":"test","environment":"PROD"}'
   ```

   Copy the `rawKey` from the response (it is only shown once).

2. **Create a flag** (as admin):

   ```bash
   curl -u admin:admin123 -X POST http://localhost:8080/api/admin/flags \
     -H "Content-Type: application/json" \
     -d '{"featureKey":"my-feature","environment":"PROD","enabled":true,"rolloutPercent":50}'
   ```

3. **Evaluate** (with your API key):

   ```bash
   curl -H "X-API-Key: YOUR_RAW_KEY" \
     "http://localhost:8080/api/flags/my-feature/evaluate?environment=PROD&userId=alice"
   ```

   Response example: `{"enabled":true,"reason":"ROLLOUT_BUCKET_42"}`.

4. **Swagger UI** — open http://localhost:8080/swagger-ui.html, log in with `admin` / `admin123`, then Authorize with Basic and/or paste an API key to try the evaluation endpoint.

---

## Configuration

Key settings in `src/main/resources/application.yml`:

- **Datasource** — default is H2 in-memory (`jdbc:h2:mem:flags`). Override for PostgreSQL in production.
- **Admin user** — `spring.security.user.name` / `spring.security.user.password` (default `admin` / `admin123`).
- **Server port** — `server.port` (default `8080`).
- **Actuator** — health, metrics, and prometheus are exposed; see `management.endpoints.web.exposure.include`.
- **H2 console** — `spring.h2.console.enabled` and `path`; disable in production.

---

## Tests

```bash
mvn test
```

---

## Documentation

Detailed docs in the `docs/` folder:

- [Environment support](docs/ENVIRONMENT_SUPPORT.md) — dev/staging/prod model and API
- [Audit log](docs/AUDIT_LOG.md) — change history and `GET .../history`
- [API keys](docs/API_KEYS.md) — create, use, revoke
- [Metrics & health](docs/METRICS_AND_HEALTH.md) — Actuator, custom metrics, Prometheus
- [OpenAPI / Swagger](docs/OPENAPI.md) — Swagger UI and security schemes

---

## License

See repository or project metadata.
