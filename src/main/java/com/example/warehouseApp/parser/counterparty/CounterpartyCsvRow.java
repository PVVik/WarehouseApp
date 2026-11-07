package com.example.warehouseApp.parser.counterparty;

import com.example.warehouseApp.model.CounterpartyType;

public record CounterpartyCsvRow(

        int lineNumber,
        String name,
        String inn,
        String kpp,
        CounterpartyType type,
        String phone,
        String contact) {
}