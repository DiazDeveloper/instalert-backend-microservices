CREATE TABLE IF NOT EXISTS notification_contacts (
 id VARCHAR(36) PRIMARY KEY, pair_key VARCHAR(73) NOT NULL UNIQUE, requester VARCHAR(36) NOT NULL, recipient VARCHAR(36) NOT NULL, status VARCHAR(20) NOT NULL, accepted_at TIMESTAMP(6)
);
CREATE TABLE IF NOT EXISTS community_notification_preferences(account_id VARCHAR(36) PRIMARY KEY, enabled INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS community_processed_alert_events(event_id VARCHAR(36) PRIMARY KEY,alert_id VARCHAR(36) NOT NULL);
CREATE TABLE IF NOT EXISTS community_alert_recipients(alert_id VARCHAR(36) NOT NULL,recipient VARCHAR(36) NOT NULL,PRIMARY KEY(alert_id,recipient));
CREATE TABLE IF NOT EXISTS community_alert_inbox(
 id VARCHAR(36) PRIMARY KEY,event_id VARCHAR(36) NOT NULL,alert_id VARCHAR(36) NOT NULL,recipient VARCHAR(36) NOT NULL,event_type VARCHAR(100) NOT NULL,payload TEXT NOT NULL,
 received_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),read_at TIMESTAMP(6),UNIQUE(event_id,recipient)
);
