package com.example.warehouseApp.dto.movement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementResponseDto {

    private LocalDate date;
    private String documentNumber;
    private String documentType;
    private String nomenclatureName;
    private String batchNumber;
    private BigDecimal qty;

    public MovementResponseDto(LocalDate date, String documentNumber, String documentType, String nomenclatureName,
                               BigDecimal qty) {
        this.date = date;
        this.documentNumber = documentNumber;
        this.documentType = documentType;
        this.nomenclatureName = nomenclatureName;
        this.qty = qty;
    }
}
