CREATE TABLE account_identities (
 identity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 legacy_iam_user_id BIGINT UNIQUE,
 email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 account_status VARCHAR(20) NOT NULL,
 token_version BIGINT NOT NULL DEFAULT 0,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE account_profiles (
 profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 legacy_profile_user_id BIGINT UNIQUE,
 display_name VARCHAR(120) NOT NULL,
 phone VARCHAR(30) NOT NULL,
 birth_date DATE,
 gender VARCHAR(40) NOT NULL,
 avatar VARCHAR(2048) NOT NULL,
 current_location VARCHAR(255) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE account_associations (
 account_id CHAR(36) PRIMARY KEY,
 identity_id BIGINT NOT NULL UNIQUE,
 profile_id BIGINT NOT NULL UNIQUE,
 CONSTRAINT fk_account_identity FOREIGN KEY (identity_id) REFERENCES account_identities(identity_id),
 CONSTRAINT fk_account_profile FOREIGN KEY (profile_id) REFERENCES account_profiles(profile_id)
);
CREATE TABLE roles (role_id SMALLINT AUTO_INCREMENT PRIMARY KEY, role_name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE identity_roles (
 identity_id BIGINT NOT NULL, role_id SMALLINT NOT NULL,
 assigned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 PRIMARY KEY(identity_id, role_id),
 CONSTRAINT fk_role_identity FOREIGN KEY(identity_id) REFERENCES account_identities(identity_id),
 CONSTRAINT fk_identity_role FOREIGN KEY(role_id) REFERENCES roles(role_id)
);
INSERT INTO roles(role_name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
