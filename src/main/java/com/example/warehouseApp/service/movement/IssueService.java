package com.example.warehouseApp.service.movement;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.dto.movement.MovementDtoIssue;
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
public class IssueService {

    private final MovementRepository movementRepository;
    private final StockBalanceService stockBalanceService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final StorageLocationService storageLocationService;
    private final BatchService batchService;
    private final SerialItemService serialItemService;
    private final DocumentNumberService documentNumberService;
    private final AuthContextService authContextService;

    @Transactional
    @LogAudit(action = "CREATE", entityType = Movement.class)
    public MovementDtoIssue createIssue(MovementDtoIssue movementDtoIssue) {
        var nomenclature = nomenclatureService.getById(movementDtoIssue.getNomenclatureId());
        var customer = counterpartyService.getById(movementDtoIssue.getCustomerId());
        var warehouse = warehouseService.getById(movementDtoIssue.getWarehouseFrom());
        var storageLocation = (movementDtoIssue.getStorageLocationId() != null)
                ? storageLocationService.getById(movementDtoIssue.getStorageLocationId())
                : null;
        var batch = movementDtoIssue.getBatchId() != null
                ? batchService.getById(movementDtoIssue.getBatchId())
                : null;
        var serialItem = movementDtoIssue.getSerialItemId() != null
                ? serialItemService.getById(movementDtoIssue.getSerialItemId())
                : null;

        stockBalanceService.checkNomenclatureExistsOnWarehouseAndPositiveQuantity(nomenclature, warehouse);
        checkQuantitySerialItem(serialItem, movementDtoIssue.getQuantity());

        var updatedSerialItem = updateSerialItemStatus(serialItem);

        var movement = IssueMapper.mapToEntity(movementDtoIssue);
        setFieldsToEntity(movement, nomenclature, customer, warehouse, storageLocation, batch, updatedSerialItem);
        movement.setUserId(authContextService.getCurrentUserId());

        var newMovement = movementRepository.save(movement);

        stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouse, storageLocation,
                movementDtoIssue.getQuantity(), movementDtoIssue.getMovementType());

        var newMovementDto = IssueMapper.mapToDto(newMovement);
        setFieldsToDto(newMovementDto, batch, updatedSerialItem);

        log.info("Оформили расход с id и номером документа {} и {}", newMovementDto.getId(), newMovementDto.getNumber());

        return newMovementDto;
    }

    private SerialItemDto updateSerialItemStatus(SerialItemDto serialItem) {
        if (serialItem != null) {
            if (serialItem.getStatus() != ItemStatus.IN_STOCK) {
                log.warn("Ошибка при попытке списать единицу, статус единицы {}", serialItem.getStatus().getDisplayName());
                throw new IllegalStateException(String.format("Серийная единица %d не может быть списана, статус — %s",
                        serialItem.getId(), serialItem.getStatus().getDisplayName()));
            }
            serialItem.setStatus(ItemStatus.IN_USE);
            serialItem = serialItemService.update(serialItem);
        }
        log.info("Обновили статус во время отгрузки");

        return serialItem;
    }

    private void checkQuantitySerialItem(SerialItemDto serialItem, BigDecimal quantity) {
        if (serialItem != null && quantity.compareTo(BigDecimal.ONE) != 0) {
            log.warn("Некорректно введенное количество серийника, может быть только 1");
            throw new ValidationException("Проверьте введенное количество, для серийных единиц оно может быть только 1");
        }
    }

    private void setFieldsToEntity(Movement movement, NomenclatureDto nomenclature, CounterpartyDto customer,
                                   WarehouseDto warehouse, StorageLocationDto storageLocation, BatchDto batch,
                                   SerialItemDto serialItem) {
        movement.setNumber(documentNumberService.generateNumber(MovementType.ISSUE));
        movement.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        movement.setWarehouseFrom(WarehouseMapper.mapToEntityWithId(warehouse));
        movement.setCounterparty(CounterpartyMapper.mapToEntityWithId(customer));
        if (storageLocation != null) {
            movement.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        }
        if (batch != null) {
            movement.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
        }
    }

    private void setFieldsToDto(MovementDtoIssue movementDtoIssue, BatchDto batch, SerialItemDto serialItem) {
        if (batch != null) {
            movementDtoIssue.setBatchId(batch.getId());
        }
        if (serialItem != null) {
            movementDtoIssue.setSerialItemId(serialItem.getId());
        }
    }

}
