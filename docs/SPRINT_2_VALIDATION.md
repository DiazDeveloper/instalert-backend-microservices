# Sprint 2 â€” local verification, 2026-10-31 (America/Lima)

## Result

Emergencies runs independently on 8083 against the exclusive `instalert_emergencies_local` MySQL schema. Gateway 8080 routes emergencies to this process; Account 8081 validates actual sessions; monolith 8082 retains only Incidents and Communities. Angular runs on 4200. Swagger runs on 8091.

| Check | Result |
|---|---|
| Emergencies integration tests | 8 passed |
| Executable Cucumber scenarios | 3 passed |
| Gateway regression tests | 3 passed |
| Monolith regression tests | 4 passed |
| Emergencies Angular transport/state tests | 8 passed |
| Account Angular regression tests | 8 passed |
| Production Angular build | Passed; existing Incidents CSS budget and Leaflet CommonJS warnings remain |
| Real local HTTP verification | 26 checks passed through Gateway + Account + MySQL |
| Browser | Login, restore active alert, cancel through Angular, reload and confirm persisted cancelled history |

The integration suite uses H2 and a clearly identified Account contract double; the HTTP verification uses real Account and MySQL. It includes session revocation, ownership denial, replay/conflict, concurrent requests, filtering, cancellation and remaining-context availability. Integration tests additionally inject an outbox storage failure and assert that alert/request/event writes roll back together, and verify that Account unavailability returns 503 without creating an alert.

Reports: `services/ms-emergencies/target/surefire-reports`, `services/ms-emergencies/target/cucumber.json`, Gateway and monolith `target/surefire-reports`, and `target/local-run/emergencies-verification.json`. Logs and credentials are ignored by Git. Re-running the HTTP script creates isolated sample accounts and alerts; it does not reset personal accounts.

Browser evidence: `D:/ProyectoArquitectura/SprintReviewEvidence/sprint2/emergency-cancelled.png`. The browser test used a sample alert created over HTTP with sample coordinates, then restored and cancelled through Angular. The same UUID survived an Emergencies restart. Browser location was denied in the test browser: history and cancellation continued to work. Actual GPS acquisition still depends on the user's browser/device and is not claimed as tested by synthetic HTTP coordinates.

## Explicit limits

- Local follow-up now delivers outbox events by authenticated HTTP to Communities, selecting accepted consenting contacts and persisting their in-app inbox. The worker retries unavailable receivers; actual recovery and 16 real-stack checks are recorded in NOTIFICATIONS_LOCAL.md. External push providers, proximity recipients and Azure Service Bus remain unimplemented.
- Owned history remains in Emergencies; received creation/cancellation notices are now available in the Communities inbox and Angular /app/avisos. Broader criteria concerning proximity, group recipients and device delivery are not claimed complete.
- No cloud deployment, throughput benchmark, AC01 p95 result or AC02 worker-recovery result is claimed.
- Original legacy and design alert tables were empty before validation. The existing design schema was preserved; no real alert migration or destructive rollback was performed.
- The full pre-existing Angular test suite is not claimed to pass. Targeted suites and the production build were run.
- Administrative UI uses the same owner-only emergency contract. An administrator does not obtain an undocumented global location-access privilege.
