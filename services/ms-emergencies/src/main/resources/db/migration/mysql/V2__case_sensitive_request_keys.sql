ALTER TABLE alert_creation_requests
    MODIFY request_key VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL;
