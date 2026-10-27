package com.example.warehouseApp.model;

import lombok.Getter;

@Getter
public enum MovementType {

    RECEIPT("Приход"),
    ISSUE("Расход"),
    TRANSFER("Перемещение"),
    WRITE_OFF("Списание");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }
}
