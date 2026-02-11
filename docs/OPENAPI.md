# OpenAPI / Swagger UI

This document describes **MVP #5: OpenAPI documentation** — how to access and use the interactive API docs.

---

## Access

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Both require **HTTP Basic auth** with an **ADMIN** user (e.g. `admin` / `admin123`), same as other admin endpoints.

---

## Using Swagger UI

1. Open **http://localhost:8080/swagger-ui.html** and log in with Basic auth when prompted (or use the **Authorize** button).
2. **Admin endpoints** use **HTTP Basic**. Click **Authorize**, choose **basicAuth**, enter `admin` / `admin123`, then **Authorize**.
3. **Evaluation endpoint** uses **X-API-Key**. To try it from Swagger UI:
   - Create an API key first: **Admin – API keys** → **POST /api/admin/api-keys** (with Basic auth), copy `rawKey` from the response.
   - Click **Authorize**, choose **apiKey**, paste the key as the value for **X-API-Key**, then **Authorize**.
4. You can then run any operation from the UI (e.g. **Evaluation** → **GET /api/flags/{featureKey}/evaluate**).

---

## What’s documented

- **Evaluation** — `GET /api/flags/{featureKey}/evaluate` (X-API-Key).
- **Admin – Feature flags** — list, get, create, update, add/remove targets, history (HTTP Basic).
- **Admin – API keys** — create, list, revoke (HTTP Basic).

Request/response schemas are generated from the Java types (e.g. `FeatureFlag`, `EvaluationResult`, `CreateFlagRequest`). Security schemes (Basic and ApiKey) are declared so the UI can send the right headers.

---

## Configuration

In **application.yml**:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
```

**SecurityConfig** allows `/swagger-ui/**` and `/v3/api-docs/**` only for users with role **ADMIN**.

---

## Files added/updated

| File | Purpose |
|------|--------|
| **pom.xml** | Dependency `springdoc-openapi-starter-webmvc-ui` (2.8.4). |
| **application.yml** | `springdoc.api-docs.path`, `swagger-ui.path`, sort options. |
| **SecurityConfig.java** | Permit `/swagger-ui/**` and `/v3/api-docs/**` for ADMIN. |
| **OpenApiConfig.java** | Bean that sets title, description, and security schemes (basicAuth, apiKey). |
| **FeatureAdminController** | `@Tag` and `@Operation` for grouping and summaries. |
| **FeatureClientController** | `@Tag` and `@Operation` for evaluation. |
| **ApiKeyAdminController** | `@Tag` and `@Operation` for API key management. |

This gives you a single place to explore and try the API for CV and interviews.
