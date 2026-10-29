package com.example.warehouseApp.service.movement;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.dto.movement.MovementDtoWriteOff;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.mapper.*;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.domain.AuthContextService;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import com.example.warehouseApp.service.storageLocation.StorageLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class WriteOffService {

    private final MovementRepository movementRepository;
    private final StockBalanceService stockBalanceService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<WarehouseDto> warehouseService;
    private final StorageLocationService storageLocationService;
    private final BatchService batchService;
    private final SerialItemService serialItemService;
    private final DocumentNumberService documentNumberService;
    private final AuthContextService authContextService;

    @Transactional
    @LogAudit(action = "CREATE", entityType = Movement.class)
    public MovementDtoWriteOff createWriteOff(MovementDtoWriteOff movementDtoWriteOff) {
        var nomenclature = nomenclatureService.getById(movementDtoWriteOff.getNomenclatureId());
        var warehouse = warehouseService.getById(movementDtoWriteOff.getWarehouseFrom());
        var storageLocation = storageLocationService.getById(movementDtoWriteOff.getStorageLocationId());
        var batch = movementDtoWriteOff.getBatchId() != null
                ? batchService.getById(movementDtoWriteOff.getBatchId())
                : null;
        var serialItem = movementDtoWriteOff.getSerialItemId() != null
                ? serialItemService.getById(movementDtoWriteOff.getSerialItemId())
                : null;

        stockBalanceService.checkNomenclatureExistsOnWarehouseAndPositiveQuantity(nomenclature, warehouse);
        checkQuantitySerialItem(serialItem, movementDtoWriteOff.getQuantity());

        var updatedSerialItem = updateSerialItemStatus(serialItem);

        var movement = WriteOffMapper.mapToEntity(movementDtoWriteOff);
        setFieldsToEntity(movement, nomenclature, warehouse, storageLocation, batch, updatedSerialItem);
        movement.setUserId(authContextService.getCurrentUserId());

        var newMovement = movementRepository.save(movement);

        stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouse, storageLocation,
                movementDtoWriteOff.getQuantity(), movementDtoWriteOff.getMovementType());

        var newMovementDto = WriteOffMapper.mapToDto(newMovement);
        setFieldsToDto(newMovementDto, batch, updatedSerialItem);

        log.info("Оформили списание с id и номером документа {} и {}", newMovementDto.getId(), newMovementDto.getNumber());

        return newMovementDto;
    }

    private void checkQuantitySerialItem(SerialItemDto serialItem, BigDecimal quantity) {
        if (serialItem != null && quantity.compareTo(BigDecimal.ONE) != 0) {
            log.warn("Некорректно введенное количество серийника, может быть только 1");
            throw new ValidationException("Проверьте введенное количество, для серийных единиц оно может быть только 1");
        }
    }

    private SerialItemDto updateSerialItemStatus(SerialItemDto serialItem) {
        if (serialItem != null) {
            if (serialItem.getStatus() != ItemStatus.IN_STOCK) {
                log.warn("Ошибка при попытке списать единицу, статус единицы {}", serialItem.getStatus().getDisplayName());
                throw new IllegalStateException(String.format("Серийная единица %d не может быть списана, статус — %s",
                        serialItem.getId(), serialItem.getStatus().getDisplayName()));
            }
            serialItem.setStatus(ItemStatus.WRITTEN_OFF);
            serialItem = serialItemService.update(serialItem);
        }
        log.info("Обновили статус во время отгрузки");

        return serialItem;
    }

    private void setFieldsToEntity(Movement movement, NomenclatureDto nomenclature, WarehouseDto warehouse,
                                   StorageLocationDto storageLocation, BatchDto batch,
                                   SerialItemDto serialItem) {
        movement.setNumber(documentNumberService.generateNumber(MovementType.WRITE_OFF));
        movement.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        movement.setWarehouseFrom(WarehouseMapper.mapToEntityWithId(warehouse));
        movement.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));

        if (batch != null) {
            movement.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
    }

    private void setFieldsToDto(MovementDtoWriteOff movementDtoWriteOff, BatchDto batch, SerialItemDto serialItem) {
        if (batch != null) {
            movementDtoWriteOff.setBatchId(batch.getId());
        }
        if (serialItem != null) {
            movementDtoWriteOff.setSerialItemId(serialItem.getId());
        }
    }
}
