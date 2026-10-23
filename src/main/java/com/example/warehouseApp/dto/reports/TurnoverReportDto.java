package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoverReportDto {

    private String group;
    private String nomenclatureName;
    private BigDecimal avgMonthlySales;
    private BigDecimal turnoverDays;
}
