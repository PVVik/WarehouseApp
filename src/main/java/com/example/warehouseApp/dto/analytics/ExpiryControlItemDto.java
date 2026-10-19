package com.example.warehouseApp.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpiryControlItemDto {

    private Long batchId;
    private String batchNumber;
    private String nomenclatureName;
    private LocalDate expiryDate;
    private String status;
}