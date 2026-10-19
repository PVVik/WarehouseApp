package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.model.Batch;
import com.example.warehouseApp.model.BatchStatus;
import org.flywaydb.core.internal.util.StringUtils;

import java.time.LocalDate;

public class BatchMapper {

    public static Batch mapToEntity(BatchDto batchDto) {
        Batch batch = new Batch(batchDto.getBatchNumber(), batchDto.getProductionDate(), batchDto.getExpiryDate());
        batch.setReceiptDate(LocalDate.now());
        batch.setStatus(BatchStatus.ACTIVE);

        return batch;
    }

    public static Batch mapToEntityWithId(BatchDto batchDto) {
        Batch batch = new Batch(batchDto.getId(), batchDto.getBatchNumber(), batchDto.getProductionDate(),
                batchDto.getExpiryDate());
        batch.setReceiptDate(LocalDate.now());

        return batch;
    }

    public static BatchDto mapToDto(Batch batch) {
        var batchDto = new BatchDto(batch.getId(), batch.getNomenclature().getId(), batch.getBatchNumber(),
                batch.getProductionDate(), batch.getExpiryDate(), batch.getReceiptDate(), batch.getStatus().toString());
        if(batch.getSupplier() != null) {
            batchDto.setSupplierId(batch.getSupplier().getId());
        }

        return batchDto;
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

        return batch;
    }
}
