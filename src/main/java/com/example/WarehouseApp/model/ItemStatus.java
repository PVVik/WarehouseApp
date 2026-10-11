package com.example.WarehouseApp.model;

public enum ItemStatus {

    IN_STOCK("На складе"),
    IN_USE("В эксплуатации"),
    IN_REPAIR("В ремонте"),
    WRITTEN_OFF("Списан");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
