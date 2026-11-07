package com.example.warehouseApp.model;

import lombok.Getter;

@Getter
public enum CounterpartyType implements LabeledEnum {

    SUPPLIER("Поставщик"),
    CUSTOMER("Заказчик");

    private final String label;

    CounterpartyType(String label) {
        this.label = label;
    }
}
