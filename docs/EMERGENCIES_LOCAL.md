# Emergencies — Sprint 2 local

## Implemented

Independent Java 21 / Spring Boot service on **8083**. Gateway forwards `/api/v1/emergencies/**` to this service and preserves Authorization, Idempotency-Key, query parameters, HTTP status and correlation IDs. Account remains on 8081; Incidents and Communities remain on the monolith on 8082. Angular uses Gateway 8080.

The monolith no longer scans emergency controllers, repositories or entities: it cannot keep writing the old alerts. Legacy source files remain for comparison/rollback, not as active components.

The service validates each session through Account (including revocation), fails closed on unavailable Account, derives the owner from that session, and restricts reading and cancellation to the owner. It stores public UUIDs, structured coordinates, UTC timestamps and ACTIVE/CANCELLED status. There is no cross-service database access or foreign key.

Creation requires `Idempotency-Key` (8–128 ASCII letters/digits/underscore/hyphen). Equivalent retries return the same UUID (200 instead of the initial 201). Different content with the same account/key returns 409. Uniqueness in MySQL handles concurrent duplicates; a conflicting transaction rolls back before retrieving the committed result. Cancellation can be repeated without another event. Optimistic locking detects concurrent updates.

Alert, creation request and outbox event commit together. A scheduled worker now delivers PENDING events over authenticated local HTTP to Communities, which persists each eligible recipient's in-app notice and deduplicates event IDs transactionally. Only then is the event marked PUBLISHED. Cancellation uses the original recipient snapshot. See [NOTIFICATIONS_LOCAL.md](NOTIFICATIONS_LOCAL.md) for consent, setup, API and verified recovery behavior. PUBLISHED means the local inbox accepted the event; it does not mean the person read it or an external provider delivered a push. External push/email/SMS, proximity recipients, cloud deployment and AC01 load targets remain outside this local implementation.

## Local database and design reconciliation

The existing `instalert_emergencies` contains the proposed SQL model without Flyway history. It was preserved. Runtime uses a separate **instalert_emergencies_local** schema with a dedicated `instalert_emergencies_app` user restricted to that schema. Flyway creates and validates the runtime tables. Credentials are in ignored `.env.local` only.

The runtime migration is a minimal implemented subset of the proposed model: alerts, creation requests and outbox events. UUID strings use VARCHAR(36), request keys and hashes are strings, domain status remains ACTIVE/CANCELLED. The runtime now additionally records delivery attempts/published time and Communities owns consent, processed events, recipient snapshots and the local inbox. Never point this V1 migration at the existing design schema or enable automatic Flyway baselining to hide a mismatch.

On 2026-10-31 both the legacy alert table and design alert table had **zero rows before execution**. No real alert import was needed or claimed. Test accounts and sample alerts created by validation remain distinguishable as test data.

Environment variables in `.env.local`:

| Variable | Local value |
|---|---|
| EMERGENCIES_DB_URL | jdbc:mysql://localhost:3306/instalert_emergencies_local |
| EMERGENCIES_DB_USER | instalert_emergencies_app |
| EMERGENCIES_DB_PASSWORD | Local generated value; never commit |
| EMERGENCIES_SERVICE_URL | http://localhost:8083 |
| ACCOUNT_SERVICE_URL | http://localhost:8081 |

The other existing Account and monolith variables remain necessary.

## Start with WebStorm terminals

Open the backend root containing `pom.xml`. MySQL must already be running.

If jars need rebuilding, stop the corresponding running Java processes first (Windows locks running jar files), then:

```powershell
.\scripts\Build-LocalStack.ps1
.\scripts\Start-LocalStack.ps1 -IncludeFrontend
```

The startup helper preserves occupied ports instead of terminating unknown processes. It starts Java and Angular in the background; check `target/local-run/*-startup.log` and `*-error.log` for completion. To see startup logs in a terminal:

```powershell
Get-Content .\target\local-run\emergencies-startup.log -Tail 25 -Wait
```

Alternatively run each backend process in its own terminal:

```powershell
.\scripts\Start-Local.ps1 -Service Account
.\scripts\Start-Local.ps1 -Service Emergencies
.\scripts\Start-Local.ps1 -Service Monolith
.\scripts\Start-Local.ps1 -Service Gateway
```

In a frontend terminal: `npm start`. Open **http://localhost:4200/login**. A real Account session is required; the existing local demo account remains available. Open Panic Button, request location, and grant browser location permission yourself. Creating an alert rechecks current coordinates. Denied/unavailable location must not produce a success message. Loading the view restores the latest active alert from the server. Cancel, then reload and check its persisted history entry.

## Swagger and Postman

From the backend root:

```powershell
python .\scripts\serve_emergencies_docs.py
```

Open **http://localhost:8091/swagger-ui/index.html**. Swagger proxies its four declared operations to Gateway, forwarding the idempotency key. Authorize with a fresh Account JWT. Use a current UTC `locationRecordedAt`; coordinates older than ten minutes or more than one minute ahead are rejected. The standalone service also exposes its specification at **http://localhost:8083/v3/api-docs**.

Import `docs/postman/emergencies.postman_collection.json`. Set its `token` variable after signing in through Account. Creation retains a generated key/timestamp for safe retries and saves `alertId`. Clear `requestKey` and `locationRecordedAt` to create a genuinely new request.

| Operation | Contract |
|---|---|
| POST /api/v1/emergencies | type, latitude, longitude, optional accuracyM, locationRecordedAt; Idempotency-Key header |
| GET /api/v1/emergencies | own history; page ≥ 0, size 1–100, optional status, type, from/to UTC |
| GET /api/v1/emergencies/{alertId} | own alert by UUID |
| PATCH /api/v1/emergencies/{alertId} | `{ "status": "CANCELLED" }`, owner only |

## Verification

- Backend integration: lifecycle, owner isolation, invalid inputs/session, revoked-session rejection through Account contract, replay/conflict, pagination, concurrent duplicates, stale coordinates, rollback on simulated outbox failure.
- Three executable Cucumber scenarios in `services/ms-emergencies/src/test/resources/features/emergencies.feature` (HU4/HU5/HU7, partial acceptance coverage).
- Gateway and monolith regression tests verify routing and ownership boundaries.
- Frontend: `npm run test:emergencies`, `npm run test:account`, `npm run build`.
- Real local system: `python scripts/verify_emergencies_local.py`. Creates two sample accounts, tests 26 HTTP checks with actual MySQL/Account/Gateway and writes a sanitized report under `target/local-run/emergencies-verification.json`. Uses no bypass or hardcoded login in production code.

Browser GPS acquisition must be verified on a device/browser where location is available. HTTP checks use clearly identified sample coordinates and do not prove real GPS accuracy, external delivery, cloud deployment or throughput targets.

## Transfer and rollback

Before importing any future non-empty legacy dataset, export a backup, map legacy owners to Account UUIDs and reconcile invalid free-text locations/states. Do not silently fabricate coordinates. Run a dry-run report and verify counts and owner mapping before switching writers.

For rollback, stop the new emergency writer and keep a backup of its alerts and pending events. Reconcile any new records before reverting code/routes to a compatible previous version. Never simply point Gateway back to an empty old table or run both writers. No destructive rollback or real-data conversion was executed in this session.
