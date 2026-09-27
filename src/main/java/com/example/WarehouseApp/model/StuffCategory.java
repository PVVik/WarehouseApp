package com.example.WarehouseApp.model;

import lombok.Getter;

@Getter
public enum StuffCategory {
    MATERIAL("Материал"),
    EQUIPMENT("Оборудование"),
    TOOL("Инструмент"),
    SIZ("СИЗ"),
    FUEL("Топливо");

    private final String label;

    StuffCategory(String label) {
        this.label = label;
    }

    public static StuffCategory fromCode(String code) {
        if (code == null) return null;
        for (StuffCategory s : values()) {
            if (s.name().equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Неизвестная категория: " + code);
    }
}
