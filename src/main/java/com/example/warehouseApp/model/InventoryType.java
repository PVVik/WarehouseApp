package com.example.warehouseApp.model;

import lombok.Getter;

@Getter
public enum InventoryType {
    BATCH("Партионный"),
    SERIAL("Серийный"),
    QUANTITY("Количественный");

    private final String label;

    InventoryType(String label) {
        this.label = label;
    }
}

