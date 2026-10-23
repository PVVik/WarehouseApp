package com.example.warehouseApp.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonLiquidReportDto {

    private String nomenclatureName;
    private String lastMovementDate;
    private Long daysWithoutMovement;
}
