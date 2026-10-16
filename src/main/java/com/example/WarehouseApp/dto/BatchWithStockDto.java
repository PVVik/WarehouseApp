package com.example.WarehouseApp.dto;

import java.math.BigDecimal;

public record BatchWithStockDto(Long id, String number, BigDecimal totalQuantity) {}

