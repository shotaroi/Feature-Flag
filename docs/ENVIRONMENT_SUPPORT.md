# Environment Support — Detailed Explanation

This document walks through **MVP #1: Environment support (dev / staging / prod)** — what it is, why it matters, and how it was implemented in this project.

---

## 1. What problem does it solve?

In real applications, the same **feature flag** often needs different values per **deployment environment**:

| Environment | Example use |
|-------------|-------------|
| **DEV**     | Turn new features on for local development; 100% rollout. |
| **STAGING** | Test with a small percentage or specific users before production. |
| **PROD**    | Gradual rollout or off until you are ready. |

Without environments, you would have only one global state per flag. You could not have e.g. `new-checkout` **on** in dev and **off** in prod. Environment support lets the same logical flag (same key) have **separate configuration per environment**.

---

## 2. Design decisions

### 2.1 Where does “environment” live?

- **Option A:** One row per flag per environment in `feature_flags` (what we did).  
  - Unique key: `(feature_key, environment)`.  
  - Simple: one table, one row = one flag in one env. Easy to query and reason about.

- **Option B:** Separate “flag definition” table and a “flag state per environment” table.  
  - More normalized but more joins and more code for an MVP.

For a junior-level MVP, **Option A** is clearer and sufficient.

### 2.2 Do targets (user allowlist) need environment?

- **No.** A target links a user to a **specific flag row**. That row already has an environment.  
- So `feature_targets` stays `(feature_flag_id, user_id)`. The environment is implied by the flag row.  
- Adding environment to targets would only be needed if you wanted different allowlists per environment (e.g. different beta users in dev vs prod). We kept it simple.

### 2.3 How do clients and admins specify environment?

- **Admin API:** For list/get/update/addTarget/removeTarget we use a **query parameter** `environment` (e.g. `?environment=PROD`). For **create**, environment is in the **request body** so one payload defines key + environment + enabled + rolloutPercent.  
- **Evaluation API:** Clients pass `environment` as a **query parameter** (e.g. `?environment=PROD&userId=alice`).  
- Using a query parameter keeps the API explicit and easy to use from scripts and other services. An alternative would be a header (e.g. `X-Environment: PROD`).

---

## 3. What was implemented (file by file)

### 3.1 Domain: `Environment` enum

**File:** `src/main/java/com/shotaroi/featureflags/domain/Environment.java`

- Values: `DEV`, `STAGING`, `PROD`.
- Used in the entity and in API request/params so we get validation and type safety (invalid values become 400 with a clear message).

### 3.2 Database: Flyway migration

**File:** `src/main/resources/db/migration/V3__add_environment_to_feature_flags.sql`

- **Add column** `environment VARCHAR(50) NOT NULL DEFAULT 'DEV'`.  
  - Existing rows (if any) get `DEV` so the migration is safe.
- **Drop** the old unique constraint on `feature_key` only.
- **Add** new unique constraint on `(feature_key, environment)`.

So the same `feature_key` can appear once per environment (e.g. `new-checkout` + `DEV`, `new-checkout` + `STAGING`, `new-checkout` + `PROD`).

### 3.3 Entity: `FeatureFlag`

**File:** `src/main/java/com/shotaroi/featureflags/domain/FeatureFlag.java`

- New field: `environment` of type `Environment`, mapped with `@Enumerated(EnumType.STRING)` so the DB stores `DEV`/`STAGING`/`PROD`.
- `@Table` unique constraint updated to `uk_feature_key_environment` on `(feature_key, environment)`.
- Getters/setters for `environment` added.

`FeatureTarget` is unchanged; it still references `FeatureFlag` by id. The environment is carried by the flag row.

### 3.4 Repository: `FeatureFlagRepository`

**File:** `src/main/java/com/shotaroi/featureflags/repository/FeatureFlagRepository.java`

- **Before:** `findByFeatureKey(String featureKey)` — one global lookup.
- **After:**
  - `findByFeatureKeyAndEnvironment(String featureKey, Environment environment)` — fetch the single flag for that key and env.
  - `findAllByEnvironment(Environment environment)` — list all flags for an environment (for admin list).

We no longer expose a global “find by key only” so that every caller is forced to think in terms of (key, environment).

### 3.5 DTOs: `AdminDtos.CreateFlagRequest`

**File:** `src/main/java/com/shotaroi/featureflags/dto/AdminDtos.java`

- `CreateFlagRequest` now includes `@NotNull Environment environment`.  
- Create payload example: `{ "featureKey": "new-checkout", "environment": "PROD", "enabled": false, "rolloutPercent": 0 }`.

### 3.6 Service: `FeatureAdminService`

**File:** `src/main/java/com/shotaroi/featureflags/service/FeatureAdminService.java`

- **get:** Takes `(featureKey, environment)` and uses `findByFeatureKeyAndEnvironment`.
- **list:** Takes `environment` and uses `findAllByEnvironment`.
- **create:** Reads `environment` from the request and sets it on the new `FeatureFlag`.
- **update:** Takes `(featureKey, environment)` to find the flag, then updates enabled/rolloutPercent.
- **addTarget / removeTarget:** Take `(featureKey, environment)` to resolve the flag row, then add/remove the target for that row.

All error messages that refer to “not found” now include the environment (e.g. “Not found: new-checkout in PROD”) so debugging is easier.

### 3.7 Service: `FeatureEvaluationService`

**File:** `src/main/java/com/shotaroi/featureflags/service/FeatureEvaluationService.java`

- **evaluate:** Signature is now `evaluate(String featureKey, Environment environment, String userId)`.
- Looks up the flag with `findByFeatureKeyAndEnvironment(featureKey, environment)`.  
- The rest of the logic (enabled, targeted user, rollout bucket) is unchanged; it just runs on that environment-specific flag.

### 3.8 Controllers

**Admin:** `FeatureAdminController.java`

- **GET /api/admin/flags** — `@RequestParam Environment environment` (required). Returns flags for that environment.
- **GET /api/admin/flags/{featureKey}** — `@RequestParam Environment environment` (required).
- **POST /api/admin/flags** — body includes `environment` (from `CreateFlagRequest`).
- **PATCH /api/admin/flags/{featureKey}** — `@RequestParam Environment environment` (required).
- **POST /api/admin/flags/{featureKey}/targets** — `@RequestParam Environment environment` (required).
- **DELETE /api/admin/flags/{featureKey}/targets/{userId}** — `@RequestParam Environment environment` (required).

**Client:** `FeatureClientController.java`

- **GET /api/flags/{featureKey}/evaluate** — `@RequestParam Environment environment` (required), optional `userId`.  
  Example: `GET /api/flags/new-checkout/evaluate?environment=PROD&userId=alice`.

If a client omits `environment` or sends an invalid value (e.g. `INVALID`), Spring returns 400 (required param missing or enum conversion failure).

### 3.9 Tests

- **FeatureEvaluationServiceTest:** All tests now pass `Environment` into `evaluate()` and use `findByFeatureKeyAndEnvironment` in mocks. The helper `flag(...)` sets `environment` on the flag.
- **SecurityIntegrationTest:**  
  - Evaluation request includes `?environment=PROD`.  
  - Admin list includes `?environment=DEV`.  
  - Create-flag body includes `"environment": "DEV"`.

---

## 4. How to use it (examples)

### Create a flag in PROD (off, 0% rollout)

```http
POST /api/admin/flags
Authorization: Basic <admin credentials>
Content-Type: application/json

{
  "featureKey": "new-checkout",
  "environment": "PROD",
  "enabled": false,
  "rolloutPercent": 0
}
```

### Create the same key in DEV (on, 100%)

```http
POST /api/admin/flags
...

{
  "featureKey": "new-checkout",
  "environment": "DEV",
  "enabled": true,
  "rolloutPercent": 100
}
```

### List all flags in PROD

```http
GET /api/admin/flags?environment=PROD
Authorization: Basic ...
```

### Evaluate for a user in PROD

```http
GET /api/flags/new-checkout/evaluate?environment=PROD&userId=alice
```

### Update PROD flag (e.g. enable 10% rollout)

```http
PATCH /api/admin/flags/new-checkout?environment=PROD
Authorization: Basic ...
Content-Type: application/json

{ "enabled": true, "rolloutPercent": 10 }
```

---

## 5. Why this is good for your CV (junior backend)

- **Data model:** You extended an existing schema with a new dimension (environment) and a migration that keeps existing data valid.
- **API design:** You made the API explicit and consistent (query param vs body) and used an enum for validation.
- **Scoping:** Every read/write is scoped by environment, so you show you think about multi-environment and avoiding cross-env mistakes.
- **Backward compatibility:** The migration uses a default so old rows get a valid environment; no manual data fix required.

If you want to extend this later, you could add a default environment (e.g. from config or header), or restrict which environments a given API key can access — but the MVP is complete and production-pattern friendly.
