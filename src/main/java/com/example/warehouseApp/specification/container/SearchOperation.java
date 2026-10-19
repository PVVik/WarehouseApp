package com.example.warehouseApp.specification.container;

public enum SearchOperation {

    EQUALITY,
    GREATER_THAN,
    LESS_THAN;

    public static SearchOperation getSimpleOperation(char ch) {
        return switch (ch) {
            case ':' -> EQUALITY;
            case '>' -> GREATER_THAN;
            case '<' -> LESS_THAN;
            default -> null;
        };
    }

}
