CREATE TABLE IF NOT EXISTS internal_inventory (
    sku VARCHAR(100) PRIMARY KEY,
    quantity INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_internal_inventory_sku
ON internal_inventory(sku);