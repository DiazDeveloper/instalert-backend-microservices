# Emergencies notifications — local implementation, 2026-10-31

## Implemented boundary and delivery

Emergencies owns alert lifecycle and its transactional outbox. Communities remains in the monolith (8082) and owns accepted trusted-contact links, preferences, event deduplication, recipient snapshots and the durable in-app inbox. No fifth service, database cross-query, external provider or email/SMS/push credentials are needed.

`Emergencies transaction → PENDING outbox → scheduled HTTP worker → Communities transaction (processed event + recipients + inbox) → 204 → PUBLISHED`.

The worker polls every second, locks the oldest pending event, sends the same immutable event ID on retries, and retains a failed attempt as PENDING. A crash after receiver commit but before sender commit is safe: the receiver acknowledges the already processed ID without duplicating the inbox. Only one event is processed per transaction to preserve creation/cancellation order. Timeouts bound each attempt. A persistently invalid event blocks later events and needs operator investigation; there is no dead-letter UI or throughput target claimed.

The internal endpoint requires a random shared service key (at least 32 characters), supplied through ignored `.env.local` to both processes. Gateway blocks the internal path, and never forwards this key from browsers. Missing/incorrect key fails closed. This local HTTP arrangement requires TLS/service credentials management before any cloud deployment.

## Consent and privacy

A directory entry is not consent. A user invites another Account UUID; only the invited user can accept. New alerts reach accepted contacts with notifications enabled, whose acceptance predates the alert creation. The latter prevents sending historical queued locations to newly accepted contacts. No contact-directory broadcast occurs. Both parties may revoke a link; either person's new alerts can then no longer reach the other.

Creation stores the exact recipients. Cancellation reaches those same recipients even if they subsequently opt out or revoke the link, because they already received that alert and need its cancellation. New opted-out or revoked recipients receive no new alert. Received notices are retained as private history; read state belongs to that recipient. The owner and unrelated accounts cannot read another user's inbox or mark its notices read.

There is no inferred proximity selection: recipients have no verified fresh location or proximity consent. Community group broadcast and mobile/browser background push are not implemented. Delivery here means an actual persisted, queryable in-app notice, displayed by Angular polling every three seconds. Read acknowledgment is separate.

## Local setup

Existing local MySQL is reused. `instalert_db` contains the Communities-owned tables; Emergencies keeps its independent `instalert_emergencies_local` database.

1. Run `scripts/Initialize-NotificationsLocal.ps1` once. It prompts for the local MySQL administrator password, creates only the five new tables if absent, grants only SELECT/INSERT/UPDATE/DELETE on those tables to the existing monolith application user, and creates the service key in ignored `.env.local` if needed. Existing rows are preserved. Runtime SQL initialization is disabled; the application has no CREATE/DROP privilege.
2. Stop the corresponding local Java processes before rebuilding JARs on Windows.
3. Run `scripts/Build-LocalStack.ps1`, wait until every build completes successfully, then `scripts/Start-LocalStack.ps1 -IncludeFrontend`.
4. Account 8081, Emergencies 8083, Communities/monolith 8082, Gateway 8080, Angular 4200. Optional `COMMUNITIES_SERVICE_URL` defaults to http://localhost:8082. `NOTIFICATIONS_WORKER_ENABLED` defaults to true.
5. Open http://localhost:4200/app/avisos. In two separate browser sessions, log in as two accounts, invite and accept a contact. Create an alert with the owner. The recipient sees its creation, then its cancellation, without refreshing. Each browser needs its own login; a shared browser profile uses a shared session.

## Public API (Gateway base http://localhost:8080/api/v1)

All require a current Account Bearer session. UUIDs are public Account IDs, not numeric legacy references.

| Method/path | Input/permission | Response |
|---|---|---|
| GET /communities/contacts | Current user's links | 200 list: id, requester, recipient, status |
| POST /communities/contacts | `{ "accountId": "recipient UUID" }`; cannot invite self | 201 link id/status; an existing pair returns its existing link |
| PUT /communities/contacts/{id}/accept | Invited recipient only, no body required | 204; 403 requester/stranger; 404 unknown; 409 revoked |
| DELETE /communities/contacts/{id} | Either party | 204; 404 unknown/unowned |
| GET /communities/notification-preferences | Current user | 200 `{ "enabled": true }`; defaults true |
| PUT /communities/notification-preferences | `{ "enabled": false }` | 204 persisted opt-out |
| GET /communities/notifications?page=0 | Own inbox only; pages of 50, page >= 0 | 200 list: id, event_id, alert_id, event_type, payload (JSON string), received_at, read_at |
| PUT /communities/notifications/{id}/read | Recipient only | 204, idempotent; 404 unowned/unknown |

Example notice: `{"id":"UUID","event_id":"UUID","alert_id":"UUID","event_type":"EmergencyAlertCancelled","payload":"{...status:CANCELLED...}","received_at":"2026-10-31T20:50:00Z","read_at":null}`. The payload is the original alert snapshot including owner Account UUID, coordinates, type, status and timestamps. A creation snapshot stays ACTIVE in historical evidence even after its separate cancellation notice arrives.

Internal POST `/api/v1/communities/internal/emergency-events` is reachable directly on 8082 only with X-Notification-Key, not through Gateway; it is not a citizen operation. Body: eventId UUID, eventType EmergencyAlertCreated/EmergencyAlertCancelled, alert object. Returns 204 after commit, including duplicate acknowledgments. Public OpenAPI is generated by the monolith at http://localhost:8082/v3/api-docs (Swagger http://localhost:8082/swagger-ui/index.html); internal injection is not a public workflow.

## Verification actually executed

- Communities: three integration tests cover consent, no pending/disabled/revoked delivery, historical event exclusion, original-recipient cancellation and deduplication.
- Emergencies: one new integration test verifies unavailable receiver leaves PENDING, successful retry marks PUBLISHED with the same event ID and two attempts. The eight previous integrations and three executable BDD scenarios still pass.
- Real local stack: `python scripts/verify_notifications_local.py` passed 16 checks with actual Account, Gateway, both service databases and Communities. Credentials/JWTs remain in memory; sanitized evidence is in ignored `target/local-run/notifications-verification.json`.
- Real outage: stopped only the locally started monolith, created a new alert successfully, observed PENDING with retries in MySQL, restarted Communities, and verified exactly one creation notice in its recipient's inbox; the outbox then became PUBLISHED. This is a single local recovery check, not a load/SLA measurement.
- Angular production build succeeds with pre-existing Incidents CSS budget and Leaflet CommonJS warnings. Browser sign-in to the recipient account confirmed both real persisted notices rendered at /app/avisos.

No cloud publication, real personal contact messaging, external-device push delivery, broker deployment, proximity notification or global HU4/HU7/HU8 completion is claimed.
