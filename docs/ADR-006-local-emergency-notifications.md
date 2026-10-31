# ADR-006: local delivery of emergency notices

Status: implemented locally, 2026-10-31; team review pending.

Context: the former Emergencies increment persisted an outbox but did not deliver notices. Legacy Communities UI listed directory entries without mutual consent. No fresh recipient geolocation or external push provider is configured.

Decision: keep Emergencies as the only alert writer and deliver outbox events by authenticated HTTP to Communities in the transitional monolith. Communities owns mutual contact acceptance, opt-out, original recipient snapshots and a durable in-app inbox. Angular polls this inbox. Stable event IDs and transactional deduplication provide at-least-once transport without duplicate notices. Acceptance must predate alert creation. Never infer consent from the public directory. Cancellation informs the creation recipients.

Consequences: fulfills local in-app creation/cancellation delivery without adding a fifth microservice or querying another service database. An outage does not roll back a created alert; pending events retry after recovery. This serial worker can block behind a permanently invalid event. TLS, provider push, broker, proximity eligibility, group membership broadcast and performance targets require separate work before corresponding criteria can be marked complete.

Traceability: Sprint 2 S2-T09/T10/T11/T12/T13/T14/T16; HU4/HU7/HU8 local notice subset, AC-03 ownership/privacy. Actual integration and recovery evidence is documented in NOTIFICATIONS_LOCAL.md.
