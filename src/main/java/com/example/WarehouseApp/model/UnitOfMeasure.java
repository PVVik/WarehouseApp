package com.example.WarehouseApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnitOfMeasure {

    METER("Метр"),
    LITER("Литр"),
    KILOGRAM("Килограмм"),
    PIECE("Штука");

    private final String label;

}

