CREATE TABLE movement (
    id BIGSERIAL PRIMARY KEY,
    movement_type VARCHAR(10) CHECK (movement_type IN ('RECEIPT', 'ISSUE', 'TRANSFER', 'WRITE_OFF')),
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    batch_id BIGINT REFERENCES batch(id),
    serial_item_id BIGINT REFERENCES serial_item(id),
    warehouse_from BIGINT REFERENCES warehouse(id),
    warehouse_to BIGINT REFERENCES warehouse(id),
    storage_location_id BIGINT REFERENCES storage_location(id),
    counterparty_id BIGINT REFERENCES counterparty(id),
    quantity DECIMAL(18, 2) NOT NULL,
    price DECIMAL(18, 2),
    document_number VARCHAR(15) NOT NULL UNIQUE,
    document_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    notes TEXT
);

CREATE TABLE stock_balance (
    id BIGSERIAL PRIMARY KEY,
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    batch_id BIGINT REFERENCES batch(id),
    serial_item_id BIGINT REFERENCES serial_item(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    storage_location_id BIGINT REFERENCES storage_location(id),
    quantity DECIMAL(18, 2) NOT NULL,
    CONSTRAINT uk_stock_balance_code UNIQUE (nomenclature_id, batch_id, serial_item_id, warehouse_id, storage_location_id)
);

CREATE TABLE document_sequence (
    id BIGSERIAL PRIMARY KEY,
    movement_type VARCHAR(10) NOT NULL,
    year INTEGER NOT NULL,
    next_value BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT uk_document_sequence_code UNIQUE (movement_type, year)
);

CREATE INDEX idx_movement_nomenclature ON movement(nomenclature_id);
CREATE INDEX idx_movement_warehouse_to ON movement(warehouse_to);
CREATE INDEX idx_movement_warehouse_from ON movement(warehouse_from);
CREATE INDEX idx_movement_date ON movement(document_date);
CREATE UNIQUE INDEX idx_movement_doc_number ON movement(document_number);