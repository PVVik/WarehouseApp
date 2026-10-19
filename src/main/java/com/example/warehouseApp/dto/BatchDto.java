package com.example.warehouseApp.dto;

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

    private LocalDate receiptDate;

    private String status;

    public BatchDto(long id, Long nomenclatureId, String batchNumber, LocalDate productionDate, LocalDate expiryDate,
                    LocalDate receiptDate, String status) {
        this.id = id;
        this.nomenclatureId = nomenclatureId;
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.receiptDate = receiptDate;
        this.status = status;
    }

    @Override
    public int compareTo(BatchDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
