CREATE TABLE batch (
    id BIGSERIAL PRIMARY KEY,
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    batch_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT REFERENCES counterparty(id),
    production_date DATE,
    expiry_date DATE,
    certificate_number VARCHAR(100),
    receipt_date DATE
);

CREATE TABLE serial_item (
    id BIGSERIAL PRIMARY KEY,
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    batch_id BIGINT REFERENCES batch(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('NEW', 'IN_STOCK', 'IN_USE', 'IN_REPAIR', 'WRITTEN_OFF')),
    passport_number VARCHAR(50) NOT NULL,
    notes VARCHAR(200)
);

CREATE INDEX idx_batch_nomenclature ON batch(nomenclature_id);
CREATE INDEX idx_batch_expiry ON batch(expiry_date);
CREATE INDEX idx_serial_nomenclature ON serial_item(nomenclature_id);
CREATE INDEX idx_serial_status ON serial_item(status);
CREATE INDEX idx_serial_batch ON serial_item(batch_id);