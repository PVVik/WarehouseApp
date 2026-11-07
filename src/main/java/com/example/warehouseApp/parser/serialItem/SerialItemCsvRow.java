package com.example.warehouseApp.parser.serialItem;

import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.Nomenclature;

public record SerialItemCsvRow(

        int lineNumber,
        Nomenclature nomenclature,
        String serialNumber,
        ItemStatus status,
        String passportNumber,
        String notes) {
}
