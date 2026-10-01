CREATE TABLE counterparty (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    inn VARCHAR(12) NOT NULL UNIQUE,
    kpp VARCHAR(9) NOT NULL,
    type VARCHAR NOT NULL CHECK (type IN ('SUPPLIER', 'CUSTOMER', 'INTERNAL')),
    phone VARCHAR(12) NOT NULL,
    contact VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE storage_location (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    zone VARCHAR(10) NOT NULL,
    rack VARCHAR(20),
    shelf VARCHAR(20),
    code VARCHAR(50) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_storage_location_code UNIQUE (warehouse_id, code)
);
