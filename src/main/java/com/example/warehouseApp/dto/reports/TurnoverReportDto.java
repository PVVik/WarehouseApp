package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoverReportDto {

    private Long nomenclatureId;
    private String nomenclatureName;
    private BigDecimal totalQuantity;
    private BigDecimal avgMonthlyQuantity;
    private BigDecimal totalAmount;
    private String group;

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
