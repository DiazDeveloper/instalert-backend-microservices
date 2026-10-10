-- Only for a NEW local Docker volume. Never run over an existing database.
CREATE TABLE incidents (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 user_id BIGINT, type VARCHAR(255), severity VARCHAR(255), address VARCHAR(255), description VARCHAR(255),
 status VARCHAR(255), latitude DOUBLE, longitude DOUBLE, time_reported VARCHAR(255)
);
CREATE TABLE emergency_alerts (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 user_id BIGINT, type VARCHAR(255), location VARCHAR(255), time VARCHAR(255), status VARCHAR(255), status_class VARCHAR(255)
);
CREATE TABLE communities (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 name VARCHAR(255), description VARCHAR(255), is_private BIT NOT NULL, owner_id BIGINT
);
