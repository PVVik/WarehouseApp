package com.example.warehouseApp.parser.nomenclature;

import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.StuffCategory;
import com.example.warehouseApp.model.UnitOfMeasure;

import java.math.BigDecimal;

public record NomenclatureCsvRow(

        int lineNumber,
        String name,
        String sku,
        StuffCategory category,
        UnitOfMeasure unit,
        InventoryType type,
        BigDecimal minStock) {
}

