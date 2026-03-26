CREATE TABLE IF NOT EXISTS deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    sender_name VARCHAR(120) NOT NULL,
    receiver_name VARCHAR(120) NOT NULL,
    receiver_phone VARCHAR(40) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    destination_address VARCHAR(500) NOT NULL,
    package_weight_kg DECIMAL(10,2) NOT NULL,
    package_type VARCHAR(64) NOT NULL,
    service_type VARCHAR(64) NOT NULL,
    quoted_price DECIMAL(10,2) NOT NULL,
    pickup_slot VARCHAR(120),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_deliveries_customer_created ON deliveries(customer_id, created_at);
CREATE INDEX idx_deliveries_status_updated ON deliveries(status, updated_at);
