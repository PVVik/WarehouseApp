package com.example.warehouseApp.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLineDto {
    private Long id;
    private String nomenclatureName;
    private String batchNumber;
    private BigDecimal accountedQty;
    private BigDecimal actualQty;

    public InventoryLineDto(Long id, String nomenclatureName, BigDecimal accountedQty, BigDecimal actualQty) {
        this.id = id;
        this.nomenclatureName = nomenclatureName;
        this.accountedQty = accountedQty;
        this.actualQty = actualQty;
    }
}
