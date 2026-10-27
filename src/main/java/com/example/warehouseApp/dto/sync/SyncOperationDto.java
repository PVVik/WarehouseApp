package com.example.warehouseApp.dto.sync;

import com.example.warehouseApp.model.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncOperationDto {

    private MovementType type;
    private String nomenclatureSku;
    private String warehouseName;
    private BigDecimal quantity;
    private LocalDate date;
    private String documentNumber;
    private String batchNumber;
    private LocalDate batchProductionDate;
    private LocalDate batchExpiryDate;
    private String serialNumber;
    private String passportNumber;
}