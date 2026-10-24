package com.example.warehouseApp.dto.reports;

import com.example.warehouseApp.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockBalanceReportDto {

    private String sku;
    private String name;
    private String batchNumber;
    private String warehouseName;
    private String storageLocationCode;
    private BigDecimal quantity;
    private UnitOfMeasure unitOfMeasure;
}
