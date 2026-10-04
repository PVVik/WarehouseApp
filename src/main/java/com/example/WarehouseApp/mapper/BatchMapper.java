package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.model.Batch;
import org.flywaydb.core.internal.util.StringUtils;

import java.time.LocalDate;

public class BatchMapper {

    public static Batch mapToEntity(BatchDto batchDto) {
        Batch batch = new Batch(batchDto.getBatchNumber(), batchDto.getProductionDate(), batchDto.getExpiryDate(),
                batchDto.getCertificateNumber());
        batch.setReceiptDate(LocalDate.now());

        return batch;
    }

    public static Batch mapToEntityWithId(BatchDto batchDto) {
        Batch batch = new Batch(batchDto.getId(), batchDto.getBatchNumber(), batchDto.getProductionDate(),
                batchDto.getExpiryDate(), batchDto.getCertificateNumber());
        batch.setReceiptDate(LocalDate.now());

        return batch;
    }

    public static BatchDto mapToDto(Batch batch) {
        return new BatchDto(batch.getId(), batch.getBatchNumber(), batch.getProductionDate(), batch.getExpiryDate(),
                batch.getCertificateNumber(), batch.getReceiptDate());
    }

    public static Batch mapToUpdateEntity(Batch batch, BatchDto batchDto) {
        if (StringUtils.hasText(batchDto.getBatchNumber())) {
            batch.setBatchNumber(batchDto.getBatchNumber());
        }
        if (batchDto.getProductionDate() != null) {
            batch.setProductionDate(batchDto.getProductionDate());
        }
        if (batchDto.getExpiryDate() != null) {
            batch.setExpiryDate(batchDto.getExpiryDate());
        }
        if (StringUtils.hasText(batchDto.getCertificateNumber())) {
            batch.setCertificateNumber(batchDto.getCertificateNumber());
        }

        return batch;
    }
}
