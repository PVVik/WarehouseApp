package com.example.WarehouseApp.service.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoTransfer;
import com.example.WarehouseApp.exception.ValidationException;
import com.example.WarehouseApp.mapper.*;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.repository.MovementRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.stockBalance.StockBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementTransferService {

    private final MovementRepository movementRepository;
    private final DocumentNumberService documentNumberService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;
    private final BaseService<BatchDto> batchService;
    private final BaseService<SerialItemDto> serialItemService;
    private final StockBalanceService stockBalanceService;

    @Transactional
    public MovementDtoTransfer createTransfer(MovementDtoTransfer movementDtoTransfer) {
        var nomenclature = nomenclatureService.getById(movementDtoTransfer.getNomenclatureId());
        var warehouseFrom = warehouseService.getById(movementDtoTransfer.getWarehouseFrom());
        var warehouseTo = warehouseService.getById(movementDtoTransfer.getWarehouseTo());
        var storageLocation = storageLocationService.getById(movementDtoTransfer.getStorageLocationId());
        var batch = movementDtoTransfer.getBatchId() != null
                ? batchService.getById(movementDtoTransfer.getBatchId())
                : null;
        var serialItem = movementDtoTransfer.getSerialItemId() != null
                ? serialItemService.getById(movementDtoTransfer.getSerialItemId())
                : null;
        stockBalanceService.checkNomenclatureExistsOnWarehouseAndPositiveQuantity(nomenclature, warehouseFrom, storageLocation);
        checkQuantitySerialItem(serialItem, movementDtoTransfer.getQuantity());

        var movement = TransferMapper.mapToEntity(movementDtoTransfer);
        var updateSerialItem = updateSerialItemStatus(serialItem);
        setFieldsToEntity(movement, nomenclature, warehouseFrom, warehouseTo, storageLocation, batch, updateSerialItem);

        var newMovement = movementRepository.save(movement);

        stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouseFrom, storageLocation,
                movement.getQuantity(), movement.getMovementType());

        var newDto = TransferMapper.mapToDto(newMovement);
        setFieldsToDto(newDto, batch, serialItem);

        return newDto;
    }

    private SerialItemDto updateSerialItemStatus(SerialItemDto serialItem) {
        if (serialItem != null) {
            if (serialItem.getStatus() != ItemStatus.IN_STOCK) {
                log.warn("Ошибка при попытке списать единицу, статус единицы {}", serialItem.getStatus().getDisplayName());
                throw new IllegalStateException(String.format("Серийная единица %d не может быть списана, статус — %s",
                        serialItem.getId(), serialItem.getStatus().getDisplayName()));
            }
            serialItem.setStatus(ItemStatus.IN_TRANSIT);
            serialItem = serialItemService.update(serialItem);
        }
        log.info("Обновили статус для транспортировки");

        return serialItem;
    }

    private void setFieldsToEntity(Movement movement, NomenclatureDto nomenclature, WarehouseDto warehouseFrom,
                                   WarehouseDto warehouseTo, StorageLocationDto storageLocation, BatchDto batch,
                                   SerialItemDto serialItem) {
        movement.setNumber(documentNumberService.generateNumber(movement.getMovementType()));
        movement.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        movement.setWarehouseFrom(WarehouseMapper.mapToEntityWithId(warehouseFrom));
        movement.setWarehouseTo(WarehouseMapper.mapToEntityWithId(warehouseTo));
        movement.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        if (batch != null) {
            movement.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
    }

    private void checkQuantitySerialItem(SerialItemDto serialItem, BigDecimal quantity) {
        if (serialItem != null && quantity.compareTo(BigDecimal.ONE) != 0) {
            log.warn("Некорректно введенное количество серийника, может быть только 1");
            throw new ValidationException("Проверьте введенное количество, для серийных единиц оно может быть только 1");
        }
    }

    private void setFieldsToDto(MovementDtoTransfer movementDtoTransfer, BatchDto batch, SerialItemDto serialItem) {
        if (batch != null) {
            movementDtoTransfer.setBatchId(batch.getId());
        }
        if (serialItem != null) {
            movementDtoTransfer.setSerialItemId(serialItem.getId());
        }
    }

}
