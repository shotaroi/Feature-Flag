# Audit Log (Flag Change History)

This document describes **MVP #2: Basic audit log** for feature flag changes — what it does and how to use it.

---

## What it does

Every **mutating** admin action is recorded in a `flag_change_logs` table:

| Action | Change type | Details example |
|--------|-------------|------------------|
| Create flag | `FLAG_CREATED` | `enabled=true, rolloutPercent=20` |
| Update flag | `FLAG_UPDATED` | `enabled: false -> true, rolloutPercent: 0 -> 10` |
| Add target user | `TARGET_ADDED` | `userId=alice` |
| Remove target user | `TARGET_REMOVED` | `userId=alice` |

Each log row stores:

- **featureKey** + **environment** — which flag (and env) was changed
- **changeType** — one of the four types above
- **changedBy** — username from the authenticated admin (HTTP Basic auth)
- **details** — short text describing what changed
- **createdAt** — when the change happened

You can then call **GET /api/admin/flags/{featureKey}/history?environment=PROD** to list all changes for that flag in that environment, newest first.

---

## API

### Get change history

```http
GET /api/admin/flags/{featureKey}/history?environment=PROD
Authorization: Basic <admin credentials>
```

**Response:** JSON array of log entries, newest first. Example:

```json
[
  {
    "id": 2,
    "featureKey": "new-checkout",
    "environment": "PROD",
    "changeType": "FLAG_UPDATED",
    "changedBy": "admin",
    "details": "enabled: false -> true, rolloutPercent: 0 -> 10",
    "createdAt": "2026-02-09T15:30:00Z"
  },
  {
    "id": 1,
    "featureKey": "new-checkout",
    "environment": "PROD",
    "changeType": "FLAG_CREATED",
    "changedBy": "admin",
    "details": "enabled=false, rolloutPercent=0",
    "createdAt": "2026-02-09T14:00:00Z"
  }
]
```

---

## Implementation notes

- **Who is “changedBy”?** The controller reads `Authentication.getName()` from Spring Security (the HTTP Basic username). If there is no auth, it uses `"anonymous"`.
- **Transaction:** Logging happens in the same transaction as the mutation. If the request rolls back, no log row is committed.
- **Indexes:** The migration adds indexes on `(feature_key, environment)` and `created_at` so history queries stay fast as the table grows.

---

## Files added/updated

| File | Purpose |
|------|---------|
| `domain/FlagChangeType.java` | Enum: FLAG_CREATED, FLAG_UPDATED, TARGET_ADDED, TARGET_REMOVED |
| `domain/FlagChangeLog.java` | JPA entity for `flag_change_logs` |
| `repository/FlagChangeLogRepository.java` | `findByFeatureKeyAndEnvironmentOrderByCreatedAtDesc` |
| `db/migration/V4__create_flag_change_logs.sql` | Creates table and indexes |
| `FeatureAdminService` | Injects change log repo; logs after create/update/addTarget/removeTarget; adds `listHistory()` |
| `FeatureAdminController` | Passes `Authentication` into service for `changedBy`; adds GET `.../history` |

This gives you a clear “who changed what, when” trail for CV and interviews.
