package com.example.WarehouseApp.specification.container;

import lombok.Getter;

@Getter
public class SpecSearchCriteria {

    private String key;
    private SearchOperation operation;
    private Object value;

    public SpecSearchCriteria(String key, SearchOperation operation, Object value) {
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

}
