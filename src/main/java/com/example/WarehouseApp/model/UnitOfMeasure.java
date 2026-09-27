package com.example.WarehouseApp.model;

import lombok.Getter;

@Getter
public enum UnitOfMeasure {

    METER("Метр", "м", "METER"),
    LITER("Литр", "л", "LITER"),
    KILOGRAM("Килограмм", "кг", "KILOGRAM"),
    PIECE("Штука", "шт", "PIECE");

    private final String fullName;
    private final String shortLabel;
    private final String code;

    UnitOfMeasure(String fullName, String shortLabel, String code) {
        this.fullName = fullName;
        this.shortLabel = shortLabel;
        this.code = code;
    }

    public static UnitOfMeasure fromCode(String code) {
        if (code == null) return null;
        for (UnitOfMeasure u : values()) {
            if (u.code.equalsIgnoreCase(code)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Неизвестная единица измерения: " + code);
    }
}

