package com.example.warehouseApp.parser.stockBalance;

import com.example.warehouseApp.model.*;

import java.math.BigDecimal;

public record StockBalanceCsvRow(

        int lineNumber,
        Nomenclature nomenclature,
        Batch batch,
        SerialItem serialItem,
        Warehouse warehouse,
        StorageLocation storageLocation,
        BigDecimal quantity) {
}
