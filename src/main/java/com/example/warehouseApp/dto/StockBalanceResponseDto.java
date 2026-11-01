package com.example.warehouseApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockBalanceResponseDto {

    private String sku;
    private String nomenclatureName;
    private String batchNumber;
    private String warehouseName;
    private BigDecimal quantity;
    private String uom;

    public StockBalanceResponseDto(String sku, String nomenclatureName, String warehouseName, BigDecimal quantity, String uom) {
        this.sku = sku;
        this.nomenclatureName = nomenclatureName;
        this.warehouseName = warehouseName;
        this.quantity = quantity;
        this.uom = uom;
    }
}