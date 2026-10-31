CREATE TABLE emergency_alerts (
 id VARCHAR(36) PRIMARY KEY, owner_account_id VARCHAR(36) NOT NULL,
 alert_type VARCHAR(100) NOT NULL, latitude DOUBLE PRECISION NOT NULL,
 longitude DOUBLE PRECISION NOT NULL, accuracy_m DOUBLE PRECISION,
 location_recorded_at TIMESTAMP(6) NOT NULL, created_at TIMESTAMP(6) NOT NULL,
 cancelled_at TIMESTAMP(6), status VARCHAR(20) NOT NULL, version BIGINT NOT NULL,
 CONSTRAINT ck_lat CHECK(latitude BETWEEN -90 AND 90),
 CONSTRAINT ck_lon CHECK(longitude BETWEEN -180 AND 180),
 CONSTRAINT ck_status CHECK(status IN ('ACTIVE','CANCELLED')),
 CONSTRAINT ck_accuracy CHECK(accuracy_m IS NULL OR accuracy_m >= 0),
 CONSTRAINT ck_cancel CHECK((status='ACTIVE' AND cancelled_at IS NULL) OR (status='CANCELLED' AND cancelled_at IS NOT NULL))
);
CREATE INDEX ix_emergency_owner_created ON emergency_alerts(owner_account_id,created_at);
CREATE TABLE alert_creation_requests (
 id VARCHAR(36) PRIMARY KEY, owner_account_id VARCHAR(36) NOT NULL,
 request_key VARCHAR(128) NOT NULL, request_hash VARCHAR(64) NOT NULL,
 alert_id VARCHAR(36) NOT NULL, created_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT uq_creation_owner_key UNIQUE(owner_account_id,request_key),
 CONSTRAINT fk_creation_alert FOREIGN KEY(alert_id) REFERENCES emergency_alerts(id)
);
CREATE TABLE emergency_outbox_events (
 id VARCHAR(36) PRIMARY KEY, alert_id VARCHAR(36) NOT NULL,
 event_type VARCHAR(100) NOT NULL, aggregate_version BIGINT NOT NULL,
 payload TEXT NOT NULL, occurred_at TIMESTAMP(6) NOT NULL,
 publication_status VARCHAR(20) NOT NULL,
 CONSTRAINT fk_outbox_alert FOREIGN KEY(alert_id) REFERENCES emergency_alerts(id),
 CONSTRAINT uq_event_version UNIQUE(alert_id,aggregate_version,event_type)
);
