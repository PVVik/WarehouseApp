package com.example.warehouseApp.dto.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriteOffLineBreakdownDto {

    private Long nomenclatureId;
    private String nomenclatureName;
    private Long batchId;
    private String batchNumber;
    private Long serialItemId;
    private String serialNumber;
    private BigDecimal quantity;
}
