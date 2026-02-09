# API Keys for Evaluation Endpoint

This document describes **MVP #3: API keys** for the evaluation endpoint — how they work and how to manage them.

---

## Overview

- **Evaluation endpoint** (`GET /api/flags/{featureKey}/evaluate?environment=...&userId=...`) now **requires** a valid API key in the `X-API-Key` header. Requests without a key or with an invalid/revoked key receive **401 Unauthorized**.
- **Admin endpoints** (including API key management) continue to use **HTTP Basic auth** (username/password) and require the **ADMIN** role.

---

## Using an API key

```http
GET /api/flags/new-checkout/evaluate?environment=PROD&userId=alice
X-API-Key: fk_1a2b3c4d5e6f...
```

If the key is valid and enabled, the response is the usual evaluation JSON. If the key is missing, invalid, or revoked, the response is **401** with a JSON body like:

```json
{"error":"Missing X-API-Key header"}
```
or
```json
{"error":"Invalid or revoked API key"}
```

---

## Admin API: Create / List / Revoke

All of these require **HTTP Basic** auth with an admin user.

### Create API key

```http
POST /api/admin/api-keys
Authorization: Basic <admin credentials>
Content-Type: application/json

{
  "name": "Production client",
  "environment": "PROD"
}
```

- **name** (required): A label for the key (e.g. "mobile-app", "backend-service").
- **environment** (optional): Can be `DEV`, `STAGING`, or `PROD`. Stored for reference only; the server does **not** currently restrict which environment a key can evaluate. (You can add that check later.)

**Response (201):**

```json
{
  "rawKey": "fk_a1b2c3d4e5f6789...",
  "apiKey": {
    "id": 1,
    "name": "Production client",
    "environment": "PROD",
    "enabled": true,
    "createdAt": "2026-02-09T16:00:00Z"
  }
}
```

**Important:** `rawKey` is returned **only once**. Store it securely; it cannot be retrieved later. Only a hash of the key is stored in the database.

### List API keys

```http
GET /api/admin/api-keys
Authorization: Basic <admin credentials>
```

Returns a JSON array of all keys (id, name, environment, enabled, createdAt). The actual key value and its hash are never returned.

### Revoke API key

```http
DELETE /api/admin/api-keys/{id}
Authorization: Basic <admin credentials>
```

Sets the key’s `enabled` flag to `false`. Future requests using that key will receive **401**. The row remains in the database for audit.

---

## Implementation details

- **Storage:** The raw key is never stored. Only a **SHA-256 hash** is saved in `api_keys.key_hash`. On validation, the incoming key is hashed and compared to the stored hash.
- **Key format:** Keys are generated as `fk_` + 32 hex characters (16 random bytes). Example: `fk_a1b2c3d4e5f6789012345678abcdef01`.
- **Filter order:** `ApiKeyAuthenticationFilter` runs before the standard username/password filter. It only applies to paths under `/api/flags`. For those requests, a valid `X-API-Key` sets the security context and no Basic auth is used for that call.

---

## Files added/updated

| File | Purpose |
|------|---------|
| `domain/ApiKey.java` | Entity: name, keyHash, environment, enabled, createdAt. `keyHash` excluded from JSON. |
| `db/migration/V5__create_api_keys.sql` | Table `api_keys` and unique index on `key_hash`. |
| `repository/ApiKeyRepository.java` | `findByKeyHashAndEnabledTrue`. |
| `service/ApiKeyService.java` | Generate key, hash, create, validate, list, revoke. |
| `config/ApiKeyAuthenticationFilter.java` | Reads `X-API-Key`, validates, returns 401 or sets context. |
| `config/SecurityConfig.java` | Registers filter; `/api/flags/**` requires `authenticated()`. |
| `controller/ApiKeyAdminController.java` | POST/GET/DELETE for create/list/revoke. |
| `dto/AdminDtos.java` | `CreateApiKeyRequest(name, environment)`. |

This gives you service-to-service auth for the evaluation API and a clear story for interviews.
