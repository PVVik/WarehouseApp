package com.example.WarehouseApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MovementType {

    RECEIPT("Приход"),
    ISSUE("Расход"),
    TRANSFER("Перемещение"),
    WRITE_OFF("Списание");

    private final String label;
}
