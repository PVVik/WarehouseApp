package com.example.WarehouseApp.model;

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

    public static InventoryType fromCode(String code) {
        if (code == null) return null;
        for (InventoryType i : values()) {
            if (i.name().equalsIgnoreCase(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип учета: " + code);
    }
}

