CREATE TABLE batch_execution (

    batch_id UUID PRIMARY KEY,

    file_name VARCHAR(255) NOT NULL,

    execution_type VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL,

    total_records INTEGER DEFAULT 0,

    processed_records INTEGER DEFAULT 0,

    failed_records INTEGER DEFAULT 0,

    batch_size INTEGER NOT NULL,

    start_time TIMESTAMP NOT NULL,

    end_time TIMESTAMP
);

CREATE TABLE vendor_inventory (

    vendor_inventory_id UUID PRIMARY KEY,

    batch_id UUID NOT NULL,

    vendor_id VARCHAR(100) NOT NULL,

    sku VARCHAR(100) NOT NULL,

    product_name VARCHAR(255) NOT NULL,

    quantity INTEGER NOT NULL,

    unit_price DECIMAL(10,2),

    reconciliation_status VARCHAR(30),

    quantity_difference INTEGER,

    remarks VARCHAR(500),

    notification_status VARCHAR(20),

    notification_time TIMESTAMP,

    upload_time TIMESTAMP NOT NULL,

    CONSTRAINT fk_vendor_batch
        FOREIGN KEY (batch_id)
        REFERENCES batch_execution(batch_id)
);

CREATE INDEX idx_vendor_batch
ON vendor_inventory(batch_id);

CREATE INDEX idx_vendor_sku
ON vendor_inventory(sku);

CREATE INDEX idx_vendor_reconciliation_status
ON vendor_inventory(reconciliation_status);

CREATE INDEX idx_vendor_notification_status
ON vendor_inventory(notification_status);