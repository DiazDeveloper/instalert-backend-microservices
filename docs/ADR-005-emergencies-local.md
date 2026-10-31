# ADR-005: Incremental extraction of Emergencies

Status: implemented locally; external notification integration remains deferred.

Account already owns public identity. The legacy emergency controller exposes numeric IDs and presentation states, stores free-text locations and lists alerts without owner isolation. The supplied SQL/C4 is a target design, not a migrated implementation.

Decision: independent Spring Boot service, exclusive local MySQL schema managed by Flyway, Account-backed session validation, owner-only UUID contracts, ACTIVE/CANCELLED lifecycle, structured recent coordinates, idempotent creation and transactional outbox. Transfer Gateway routes and remove emergency components from monolith scanning. Adapt Angular explicitly; do not accept spoofable owner IDs or silently convert numeric IDs. Preserve the user's existing design schema.

Consequences: API contract intentionally changes for Emergencies; Angular is adapted in the same local change. Received alerts, proximity selection, accepted contacts, broker publishing, provider delivery and cloud deployment are not implemented. PENDING outbox demonstrates durable intent, not notification delivery. Rollback requires reconciliation if new data exists. The old schema was empty at inspection, so no legacy import is claimed.

Drivers: HU4/HU5/HU7/HU8 (partial story criteria), AC03; foundations for AC01/AC02, with no performance/recovery claim.
