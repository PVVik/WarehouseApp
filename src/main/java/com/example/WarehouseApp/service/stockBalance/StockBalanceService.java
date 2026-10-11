package com.example.WarehouseApp.service.stockBalance;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.exception.ValidationException;
import com.example.WarehouseApp.mapper.*;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.model.StockBalance;
import com.example.WarehouseApp.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;

    @Transactional
    public StockBalance saveStockBalance(NomenclatureDto nomenclature, BatchDto batch, SerialItemDto serialItem,
                                         WarehouseDto warehouse, StorageLocationDto storageLocation, BigDecimal quantity,
                                         MovementType movementType) {
        StockBalance stockBalance = batch != null
                ? getByNomenclatureIdAndBatchIdAndWarehouseId(nomenclature.getId(), batch.getId(), warehouse.getId())
                : getByNomenclatureIdAndWarehouseId(nomenclature.getId(), warehouse.getId());

        if (stockBalance == null) {
            stockBalance = create(nomenclature, batch, serialItem, warehouse, storageLocation, quantity);
        } else {
            BigDecimal oldQuantity = stockBalance.getQuantity();
            if (movementType == MovementType.RECEIPT) {
                stockBalance.setQuantity(oldQuantity.add(quantity));
            }
            if (movementType == MovementType.ISSUE) {
                checkBalance(stockBalance, quantity);
                stockBalance.setQuantity(oldQuantity.subtract(quantity));
            }
            stockBalance = update(stockBalance);
        }

        return stockBalance;
    }

    private StockBalance getByNomenclatureIdAndWarehouseId(long nomenclatureId, long warehouseId) {
        return stockBalanceRepository.findByNomenclatureIdAndWarehouseId(nomenclatureId, warehouseId).orElse(null);
    }

    private StockBalance getByNomenclatureIdAndBatchIdAndWarehouseId(long nomenclatureId, long batchId, long warehouseId) {
        return stockBalanceRepository.findByNomenclatureIdAndBatchIdAndWarehouseId(nomenclatureId, batchId, warehouseId)
                .orElse(null);
    }

    private void checkBalance(StockBalance stockBalance, BigDecimal quantity) {
        if (stockBalance.getQuantity().compareTo(quantity) < 0) {
            log.warn("Введенное количество больше, чем остаток");
            throw new ValidationException("Вы пытаетесь списать больше, чем есть на складе. Проверьте данные.");
        }
    }

    private StockBalance create(NomenclatureDto nomenclature, BatchDto batch, SerialItemDto serialItem,
                                WarehouseDto warehouse, StorageLocationDto storageLocation, BigDecimal quantity) {
        StockBalance stockBalance = new StockBalance();
        stockBalance.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        stockBalance.setWarehouse(WarehouseMapper.mapToEntityWithId(warehouse));
        stockBalance.setQuantity(quantity);
        if (batch != null) {
            stockBalance.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            stockBalance.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
        if (storageLocation != null) {
            stockBalance.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        }
        return stockBalanceRepository.save(stockBalance);
    }

    private StockBalance update(StockBalance stockBalance) {
        return stockBalanceRepository.save(stockBalance);
    }
}
