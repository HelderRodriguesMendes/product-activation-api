CREATE TABLE activation_requests (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_activation_requests_external_id UNIQUE (external_id)
);