package com.example.warehouseApp.dto.inventory;

import com.example.warehouseApp.model.DiscrepancyType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DiscrepancyLineDto {
    private Long nomenclatureId;
    private String nomenclatureName;
    private String batchNumber;
    private BigDecimal accountedQty;
    private BigDecimal actualQty;
    private BigDecimal difference;
    private DiscrepancyType type;

    public DiscrepancyLineDto(Long nomenclatureId, String nomenclatureName, BigDecimal accountedQty,
                              BigDecimal actualQty, BigDecimal difference, DiscrepancyType type) {
        this.nomenclatureId = nomenclatureId;
        this.nomenclatureName = nomenclatureName;
        this.accountedQty = accountedQty;
        this.actualQty = actualQty;
        this.difference = difference;
        this.type = type;
    }
}