package com.example.warehouseApp.model;

import lombok.Getter;

@Getter
public enum UnitOfMeasure {

    METER("Метр"),
    LITER("Литр"),
    KILOGRAM("Килограмм"),
    PIECE("Штука");

    private final String label;

    UnitOfMeasure(String label) {
        this.label = label;
    }
}

