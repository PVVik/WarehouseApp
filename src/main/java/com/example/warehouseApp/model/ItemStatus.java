package com.example.warehouseApp.model;

public enum ItemStatus {

    IN_STOCK("На складе"),
    IN_USE("В эксплуатации"),
    IN_REPAIR("В ремонте"),
    WRITTEN_OFF("Списан"),
    IN_TRANSIT("В перемещении");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
