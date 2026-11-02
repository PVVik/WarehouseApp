package com.example.warehouseApp.model;

import lombok.Getter;

@Getter
public enum ItemStatus implements LabeledEnum {

    IN_STOCK("На складе"),
    IN_USE("В эксплуатации"),
    IN_REPAIR("В ремонте"),
    WRITTEN_OFF("Списан"),
    IN_TRANSIT("В перемещении");

    private final String label;

    ItemStatus(String label) {
        this.label = label;
    }
}
