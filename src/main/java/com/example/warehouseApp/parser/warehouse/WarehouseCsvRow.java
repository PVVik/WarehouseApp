package com.example.warehouseApp.parser.warehouse;

import com.example.warehouseApp.model.WarehouseType;

public record WarehouseCsvRow(

        int lineNumber,
        String name,
        String address,
        WarehouseType type) {
}
