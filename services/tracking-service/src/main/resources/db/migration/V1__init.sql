CREATE TABLE IF NOT EXISTS tracking_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    tracking_number VARCHAR(64) NOT NULL,
    event_code VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    event_time DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    tracking_number VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(128) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS delivery_proofs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL UNIQUE,
    proof_type VARCHAR(64) NOT NULL,
    proof_url VARCHAR(500) NOT NULL,
    recipient_name VARCHAR(120) NOT NULL,
    confirmed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_tracking_number_time ON tracking_events(tracking_number, event_time);
CREATE INDEX idx_tracking_delivery_time ON tracking_events(delivery_id, event_time);
