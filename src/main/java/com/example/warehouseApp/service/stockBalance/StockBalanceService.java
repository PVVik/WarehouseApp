package com.example.warehouseApp.service.stockBalance;

import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.mapper.*;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.model.StockBalance;
import com.example.warehouseApp.repository.StockBalanceRepository;
import com.example.warehouseApp.service.BaseService;
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
    private final BaseService<WarehouseDto> warehouseService;

    @Transactional
    public StockBalance saveStockBalance(NomenclatureDto nomenclature, BatchDto batch, SerialItemDto serialItem,
                                         WarehouseDto warehouse, StorageLocationDto storageLocation, BigDecimal quantity,
                                         MovementType movementType) {
        StockBalance stockBalance = batch != null
                ? getByNomenclatureIdAndBatchIdAndWarehouseId(nomenclature.getId(), batch.getId(),
                warehouse.getId())
                : getByNomenclatureIdAndWarehouseIdAndStorageLocationId(nomenclature.getId(), warehouse.getId());

        if (stockBalance == null) {
            stockBalance = create(nomenclature, batch, serialItem, warehouse, storageLocation, quantity);
        } else {
            BigDecimal oldQuantity = stockBalance.getQuantity();
            if (movementType == MovementType.RECEIPT) {
                stockBalance.setQuantity(oldQuantity.add(quantity));
            }
            if (movementType == MovementType.ISSUE || movementType == MovementType.TRANSFER || movementType == MovementType.WRITE_OFF) {
                checkBalance(stockBalance, quantity);
                stockBalance.setQuantity(oldQuantity.subtract(quantity));
            }
            stockBalance = update(stockBalance);
        }

        return stockBalance;
    }

    public List<StockBalanceResponseDto> getBalances(Long warehouseId) {
        var warehouse = warehouseService.getById(warehouseId);
        log.info("Вернули дто остатков со склада {}", warehouse.getId());

        return findPositiveBalancesByWarehouse(warehouse.getId()).stream()
                .map(StockBalanceMapper::mapToDto)
                .toList();
    }

    public List<StockBalance> findPositiveBalancesByWarehouse(Long warehouseId) {
        var warehouse = warehouseService.getById(warehouseId);
        log.info("Вернули остатки со склада {}", warehouse.getId());

        return stockBalanceRepository.findPositiveBalancesByWarehouse(warehouseId);
    }

    public void checkNomenclatureExistsOnWarehouseAndPositiveQuantity(NomenclatureDto nomenclature, WarehouseDto warehouse) {
        StockBalance stockBalance = stockBalanceRepository.findByNomenclatureIdAndWarehouseId(
                nomenclature.getId(), warehouse.getId()).orElseGet(() -> {
            log.warn("На складе с id {} не найдена позиция с id {}", warehouse.getId(), nomenclature.getId());
            throw new NotFoundException(String.format("На выбранном складе нет номенклатуры %s", nomenclature.getName()));
        });

        if (stockBalance.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("На складе с id {} не найдена позиция с id {}", warehouse.getId(), nomenclature.getId());
            throw new NotFoundException(String.format("На выбранном складе нет номенклатуры %s", nomenclature.getName()));
        }
    }

    private StockBalance getByNomenclatureIdAndWarehouseIdAndStorageLocationId(long nomenclatureId, long warehouseId) {
        return stockBalanceRepository.findByNomenclatureIdAndWarehouseId(
                nomenclatureId, warehouseId).orElse(null);
    }

    private StockBalance getByNomenclatureIdAndBatchIdAndWarehouseId(long nomenclatureId, long batchId, long warehouseId) {
        return stockBalanceRepository.findByNomenclatureIdAndBatchIdAndWarehouseId(
                        nomenclatureId, batchId, warehouseId)
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
