package com.example.WarehouseApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StuffCategory {
    MATERIAL("Материал"),
    EQUIPMENT("Оборудование"),
    TOOL("Инструмент"),
    SIZ("СИЗ"),
    FUEL("Топливо");

    private final String label;

}
