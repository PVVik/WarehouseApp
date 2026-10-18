package com.example.WarehouseApp.service.stockBalance;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.exception.NotFoundException;
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
import java.util.List;

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
                ? getByNomenclatureIdAndBatchIdAndWarehouseId(nomenclature.getId(), batch.getId(),
                warehouse.getId(), storageLocation.getId())
                : getByNomenclatureIdAndWarehouseIdAndStorageLocationId(nomenclature.getId(), warehouse.getId(),
                storageLocation.getId());

        if (stockBalance == null) {
            stockBalance = create(nomenclature, batch, serialItem, warehouse, storageLocation, quantity);
        } else {
            BigDecimal oldQuantity = stockBalance.getQuantity();
            if (movementType == MovementType.RECEIPT) {
                stockBalance.setQuantity(oldQuantity.add(quantity));
            }
            if (movementType == MovementType.ISSUE || movementType == MovementType.TRANSFER) {
                checkBalance(stockBalance, quantity);
                stockBalance.setQuantity(oldQuantity.subtract(quantity));
            }
            stockBalance = update(stockBalance);
        }

        return stockBalance;
    }

    public List<StockBalance> findPositiveBalancesByWarehouse(Long warehouseId) {
        return stockBalanceRepository.findPositiveBalancesByWarehouse(warehouseId);
    }

    public void checkNomenclatureExistsOnWarehouseAndPositiveQuantity(NomenclatureDto nomenclature, WarehouseDto warehouse,
                                                                      StorageLocationDto storageLocation) {
        StockBalance stockBalance = stockBalanceRepository.findByNomenclatureIdAndWarehouseIdAndStorageLocationId(
                nomenclature.getId(), warehouse.getId(), storageLocation.getId()).orElseGet(() -> {
            log.warn("На складе с id {} не найдена позиция с id {}", warehouse.getId(), nomenclature.getId());
            throw new NotFoundException(String.format("На выбранном складе нет номенклатуры %s", nomenclature.getName()));
        });

        if (stockBalance.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("На складе с id {} не найдена позиция с id {}", warehouse.getId(), nomenclature.getId());
            throw new NotFoundException(String.format("На выбранном складе нет номенклатуры %s", nomenclature.getName()));
        }
    }

    private StockBalance getByNomenclatureIdAndWarehouseIdAndStorageLocationId(long nomenclatureId, long warehouseId,
                                                                               long storageLocationId) {
        return stockBalanceRepository.findByNomenclatureIdAndWarehouseIdAndStorageLocationId(
                nomenclatureId, warehouseId, storageLocationId).orElse(null);
    }

    private StockBalance getByNomenclatureIdAndBatchIdAndWarehouseId(long nomenclatureId, long batchId, long warehouseId,
                                                                     long storageLocationId) {
        return stockBalanceRepository.findByNomenclatureIdAndBatchIdAndWarehouseIdAndStorageLocationId(
                        nomenclatureId, batchId, warehouseId, storageLocationId)
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
