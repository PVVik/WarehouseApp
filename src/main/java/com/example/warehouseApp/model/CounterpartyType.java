package com.example.warehouseApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CounterpartyType {

    SUPPLIER("Поставщик"),
    CUSTOMER("Заказчик");

    private final String label;

}
