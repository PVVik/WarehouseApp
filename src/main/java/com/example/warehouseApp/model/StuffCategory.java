package com.example.warehouseApp.model;

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
}
