package com.example.warehouseApp.parser.batch;

import com.example.warehouseApp.model.Counterparty;
import com.example.warehouseApp.model.Nomenclature;

import java.time.LocalDate;

public record BatchCsvRow(

        int lineNumber,
        Nomenclature nomenclature,
        String batchNumber,
        Counterparty supplier,
        LocalDate productionDate,
        LocalDate expiryDate) {
}
