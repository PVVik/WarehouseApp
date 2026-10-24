package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonLiquidReportDto {

    private Long id;
    private String nomenclatureName;
    private String sku;
    private String warehouseName;
    private String batchNumber;
    private BigDecimal currentStock;
    private BigDecimal minThreshold;
    private LocalDate lastMovementDate;
    private int daysWithoutMovement;
    private boolean isCritical;
    private String status;
}