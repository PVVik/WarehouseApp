CREATE TABLE inventory_check (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')),
    user_id BIGINT NOT NULL,
    completed_at DATE
);

CREATE TABLE inventory_line (
    id BIGSERIAL PRIMARY KEY,
    inventory_check_id BIGINT NOT NULL REFERENCES inventory_check(id) ON DELETE CASCADE,
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    batch_id BIGINT REFERENCES batch(id),
    accounted_qty DECIMAL(18, 2) NOT NULL DEFAULT 0,
    actual_qty DECIMAL(18, 2),
    difference DECIMAL(18, 2),
    note TEXT
);

CREATE TABLE inventory_discrepancy_act (
    id BIGSERIAL PRIMARY KEY,
    check_id BIGINT NOT NULL REFERENCES inventory_check(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')) DEFAULT 'DRAFT',
    created_at DATE NOT NULL DEFAULT NOW(),
    approved_at DATE
);

CREATE TABLE discrepancy_line (
    id BIGSERIAL PRIMARY KEY,
    act_id BIGINT NOT NULL REFERENCES inventory_discrepancy_act(id) ON DELETE CASCADE,
    nomenclature_id BIGINT NOT NULL REFERENCES nomenclature(id),
    batch_id BIGINT REFERENCES batch(id),
    accounted_qty DECIMAL(18,6) NOT NULL,
    actual_qty DECIMAL(18,6) NOT NULL,
    difference DECIMAL(18,6) NOT NULL,
    type VARCHAR(20) NOT NULL
);

CREATE TABLE write_off_document (
    id BIGSERIAL PRIMARY KEY,
    act_id BIGINT NOT NULL REFERENCES inventory_discrepancy_act(id) ON DELETE CASCADE,
    reason TEXT,
    quantity DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')) DEFAULT 'DRAFT',
    created_at DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE receipt_document (
    id BIGSERIAL PRIMARY KEY,
    act_id BIGINT NOT NULL REFERENCES inventory_discrepancy_act(id) ON DELETE CASCADE,
    source TEXT,
    quantity DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')) DEFAULT 'DRAFT',
    created_at DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_check_warehouse ON inventory_check(warehouse_id);
CREATE INDEX idx_inventory_check_status ON inventory_check(status);
CREATE INDEX idx_inventory_check_user ON inventory_check(user_id);
CREATE INDEX idx_inventory_line_check ON inventory_line(inventory_check_id);
CREATE INDEX idx_inventory_line_nomenclature ON inventory_line(nomenclature_id);
CREATE INDEX idx_inventory_line_batch ON inventory_line(batch_id);