package com.example.warehouseApp.dto;

import java.math.BigDecimal;

public record BatchWithStockDto(Long id, String number, BigDecimal totalQuantity) {}

