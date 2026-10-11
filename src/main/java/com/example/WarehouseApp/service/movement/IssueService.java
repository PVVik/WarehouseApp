package com.example.WarehouseApp.service.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.mapper.*;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.repository.MovementRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.stockBalance.StockBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueService {

    private final MovementRepository movementRepository;
    private final StockBalanceService stockBalanceService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;
    private final BaseService<BatchDto> batchService;
    private final BaseService<SerialItemDto> serialItemService;
    private final DocumentNumberService documentNumberService;

    @Transactional
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

        SerialItemDto updatedSerialItem = updateSerialItemStatus(serialItem);

        Movement movement = IssueMapper.mapToEntity(movementDtoIssue);
        setFieldsToEntity(movement, nomenclature, customer, warehouse, storageLocation, batch, updatedSerialItem);

        Movement newMovement = movementRepository.save(movement);

        stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouse, storageLocation,
                movementDtoIssue.getQuantity(), movementDtoIssue.getMovementType());

        MovementDtoIssue newMovementDto = IssueMapper.mapToDto(newMovement);
        setFieldsToDto(newMovementDto, batch, updatedSerialItem);

        log.info("Оформили списание с id и номером документа {} и {}", newMovementDto.getId(), newMovementDto.getNumber());

        return newMovementDto;
    }

    @Transactional(readOnly = true)
    public List<LocationBalanceDto> getLocationsWithBalance(Long nomenclatureId, Long warehouseId) {
        List<LocationBalanceDto> locationBalanceDtos =  movementRepository.findLocationsWithPositiveBalance(nomenclatureId, warehouseId);

        log.info(locationBalanceDtos.toString());

        return locationBalanceDtos;
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

        return serialItem;
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
