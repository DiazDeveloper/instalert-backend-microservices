ALTER TABLE emergency_outbox_events ADD COLUMN delivery_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE emergency_outbox_events ADD COLUMN published_at TIMESTAMP(6);
CREATE INDEX ix_outbox_pending ON emergency_outbox_events(publication_status,occurred_at);
