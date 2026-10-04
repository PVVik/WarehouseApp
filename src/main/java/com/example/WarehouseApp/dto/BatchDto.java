package com.example.WarehouseApp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BatchDto implements Comparable<BatchDto> {

    private long id;

    @NotNull(message = "Id позиции в партии обязателен")
    private Long nomenclatureId;

    @NotNull(message = "Номер партии обязателен")
    private String batchNumber;

    private Long supplierId;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private String certificateNumber;

    private LocalDate receiptDate;

    public BatchDto(long id, String batchNumber, LocalDate productionDate, LocalDate expiryDate, String certificateNumber,
                    LocalDate receiptDate) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.certificateNumber = certificateNumber;
        this.receiptDate = receiptDate;
    }

    @Override
    public int compareTo(BatchDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
