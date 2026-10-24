package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeficitReportDto {

    private String nomenclatureName;
    private BigDecimal currentStock;
    private BigDecimal minThreshold;
    private DeficitLevel level;
    private BigDecimal deficitAmount;

    public enum DeficitLevel {
        CRITICAL,
        WARNING
    }

}
