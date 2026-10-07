package com.example.WarehouseApp.service.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.mapper.*;
import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.model.StockBalance;
import com.example.WarehouseApp.repository.MovementRepository;
import com.example.WarehouseApp.repository.StockBalanceRepository;
import com.example.WarehouseApp.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReceiptService {

    private final DocumentNumberService documentNumberService;
    private final MovementRepository movementRepository;
    private final BaseService<BatchDto> batchService;
    private final BaseService<SerialItemDto> serialItemService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;
    private final StockBalanceRepository stockBalanceRepository;

    @Transactional
    public MovementDtoReceipt createReceipt(MovementDtoReceipt movementDtoReceipt) {
        var nomenclature = nomenclatureService.getById(movementDtoReceipt.getNomenclatureId());
        var supplier = counterpartyService.getById(movementDtoReceipt.getCounterpartyId());
        var warehouse = warehouseService.getById(movementDtoReceipt.getWarehouseTo());
        var storageLocation = (movementDtoReceipt.getStorageLocationId() != null)
                ? storageLocationService.getById(movementDtoReceipt.getStorageLocationId())
                : null;
        var batch = saveBatch(movementDtoReceipt.getBatchDto(), nomenclature, supplier);
        var serialItem = saveSerialItem(movementDtoReceipt.getSerialItemDto(), batch, nomenclature);

        var movement = ReceiptMapper.mapToEntity(movementDtoReceipt);
        setFieldsToEntity(movement, nomenclature, warehouse, supplier, batch, serialItem, storageLocation);

        var newMovement = movementRepository.save(movement);

        saveStockBalance(nomenclature, batch, serialItem, warehouse, storageLocation, movementDtoReceipt.getQuantity());

        var newMovementDto = ReceiptMapper.mapToDto(newMovement);
        setFieldsToDto(newMovementDto, batch, serialItem);

        return newMovementDto;
    }

    private void saveStockBalance(NomenclatureDto nomenclature, BatchDto batch, SerialItemDto serialItem,
                                  WarehouseDto warehouse, StorageLocationDto storageLocation, BigDecimal quantity) {
        var stockBalance = new StockBalance();
        if (batch != null) {
            stockBalance.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            stockBalance.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
        if (storageLocation != null) {
            stockBalance.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        }
        stockBalance.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        stockBalance.setWarehouse(WarehouseMapper.mapToEntityWithId(warehouse));
        stockBalance.setQuantity(quantity);

        stockBalanceRepository.save(stockBalance);
    }

    private BatchDto saveBatch(BatchDto batch, NomenclatureDto nomenclature, CounterpartyDto supplier) {
        if (batch != null) {
            batch.setNomenclatureId(nomenclature.getId());
            batch.setSupplierId(supplier.getId());
            batch = batchService.create(batch);

            return batch;
        } else return null;
    }

    private SerialItemDto saveSerialItem(SerialItemDto serialItem, BatchDto batch, NomenclatureDto nomenclature) {
        if (serialItem != null) {
            if (batch != null) {
                serialItem.setBatchId(batch.getId());
            }
            serialItem.setNomenclatureId(nomenclature.getId());
            serialItem = serialItemService.create(serialItem);

            return serialItem;
        } else return null;
    }

    private void setFieldsToEntity(Movement movement, NomenclatureDto nomenclature, WarehouseDto warehouse,
                                   CounterpartyDto supplier, BatchDto batch, SerialItemDto serialItem,
                                   StorageLocationDto storageLocation) {
        movement.setNumber(documentNumberService.generateNumber(movement.getMovementType()));
        movement.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        movement.setWarehouseTo(WarehouseMapper.mapToEntityWithId(warehouse));
        movement.setCounterparty(CounterpartyMapper.mapToEntityWithId(supplier));
        if (batch != null) {
            movement.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
        if (storageLocation != null) {
            movement.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        }

    }

    private void setFieldsToDto(MovementDtoReceipt movementDtoReceipt, BatchDto batch, SerialItemDto serialItem) {
        if (batch != null) {
            movementDtoReceipt.setBatchDto(batch);
        }
        if (serialItem != null) {
            movementDtoReceipt.setSerialItemDto(serialItem);
        }
    }

}
