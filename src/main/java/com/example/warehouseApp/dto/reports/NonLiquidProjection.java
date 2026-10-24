package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NonLiquidProjection {

    private Long nomenclatureId;
    private String nomenclatureName;
    private String sku;
    private String warehouseName;
    private String batchNumber;
    private BigDecimal currentStock;
    private BigDecimal minThreshold;
    private LocalDate lastMovementDate;
}