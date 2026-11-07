package com.example.warehouseApp.service.stockBalance;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.mapper.*;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.model.StockBalance;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.stockBalance.StockBalanceCsvRow;
import com.example.warehouseApp.repository.StockBalanceRepository;
import com.example.warehouseApp.repository.StorageLocationRepository;
import com.example.warehouseApp.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;
    private final BaseService<WarehouseDto> warehouseService;
    private final StorageLocationRepository storageLocationRepository;

    @Transactional
    public ImportResult importAll(List<StockBalanceCsvRow> rows) {
        int created = 0;
        int updated = 0;

        for (StockBalanceCsvRow row : rows) {
            Optional<StockBalance> existing = stockBalanceRepository
                    .findByNomenclatureIdAndWarehouseIdAndBatchIdAndSerialItemId(
                            row.nomenclature().getId(),
                            row.warehouse().getId(),
                            row.batch() != null ? row.batch().getId() : null,
                            row.serialItem() != null ? row.serialItem().getId() : null);

            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity().add(row.quantity()));
                if (row.storageLocation() != null) {
                    existing.get().setStorageLocation(row.storageLocation());
                }
                updated++;
            } else {
                StockBalance balance = new StockBalance();
                balance.setNomenclature(row.nomenclature());
                balance.setBatch(row.batch());
                balance.setSerialItem(row.serialItem());
                balance.setWarehouse(row.warehouse());
                balance.setStorageLocation(row.storageLocation());
                balance.setQuantity(row.quantity());
                stockBalanceRepository.save(balance);
                created++;
            }

            if (row.storageLocation() != null && !row.storageLocation().isUsed()) {
                row.storageLocation().setUsed(true);
                storageLocationRepository.save(row.storageLocation());
            }
        }
        return new ImportResult(created, updated);
    }

    @Transactional
    @LogAudit(action = "CREATE", entityType = StockBalance.class)
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

    public void checkBatchExistsAndPositiveQuantity(Long batchId, BigDecimal requiredQty) {
        var balance = stockBalanceRepository.findByBatchId(batchId).orElseThrow(() ->
                new NotFoundException(String.format("Нет остатков для партии %d", batchId)));

        if (balance.getQuantity() == null || balance.getQuantity().compareTo(requiredQty) < 0) {
            throw new ValidationException("Недостаточно остатка по выбранной партии");
        }
    }

    public void checkNomenclatureExistsOnWarehouseAndPositiveQuantity(NomenclatureDto nomenclature, WarehouseDto warehouse) {
        var stockBalance = stockBalanceRepository.findByNomenclatureIdAndWarehouseId(
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
