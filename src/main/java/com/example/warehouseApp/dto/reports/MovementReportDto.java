package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementReportDto {

    private LocalDate date;
    private String documentType;
    private String documentNumber;
    private String nomenclatureName;
    private BigDecimal qty;
}
