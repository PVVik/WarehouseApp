package com.example.WarehouseApp.specification.container;

import lombok.Getter;

@Getter
public class SpecSearchCriteria {

    private final String key;
    private final SearchOperation operation;
    private final Object value;

    public SpecSearchCriteria(String key, SearchOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

}
