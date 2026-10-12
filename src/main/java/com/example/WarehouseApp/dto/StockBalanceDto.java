package com.example.WarehouseApp.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class StockBalanceDto {

    private long id;

    @NotNull(message = "Номенклатура обязательна к заполнению")
    private NomenclatureDto nomenclature;

    private BatchDto batch;

    private SerialItemDto serialItem;

    @NotNull(message = "Склад обязателен к заполнению")
    private WarehouseDto warehouse;

    private StorageLocationDto storageLocation;

    private BigDecimal quantity;
}
