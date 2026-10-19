package com.example.warehouseApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryType {
    BATCH("Партионный"),
    SERIAL("Серийный"),
    QUANTITY("Количественный");

    private final String label;

}

