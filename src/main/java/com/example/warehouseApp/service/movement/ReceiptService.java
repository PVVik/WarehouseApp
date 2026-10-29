package com.example.warehouseApp.service.movement;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.mapper.*;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.Movement;
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
public class ReceiptService {

    private final DocumentNumberService documentNumberService;
    private final MovementRepository movementRepository;
    private final BatchService batchService;
    private final SerialItemService serialItemService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final StorageLocationService storageLocationService;
    private final StockBalanceService stockBalanceService;
    private final AuthContextService authContextService;

    @Transactional
    @LogAudit(action = "CREATE", entityType = Movement.class)
    public MovementDtoReceipt createReceipt(MovementDtoReceipt movementDtoReceipt) {
        var nomenclature = nomenclatureService.getById(movementDtoReceipt.getNomenclatureId());
        var supplier = counterpartyService.getById(movementDtoReceipt.getCounterpartyId());
        var warehouse = warehouseService.getById(movementDtoReceipt.getWarehouseTo());
        var storageLocation = storageLocationService.getById(movementDtoReceipt.getStorageLocationId());
        var batch = saveBatch(movementDtoReceipt.getBatchDto(), nomenclature, supplier);
        var serialItem = saveSerialItem(movementDtoReceipt.getSerialItemDto(), batch, nomenclature);
        checkSerialItemQuantity(movementDtoReceipt, nomenclature);

        var movement = ReceiptMapper.mapToEntity(movementDtoReceipt);
        setFieldsToEntity(movement, nomenclature, warehouse, supplier, batch, serialItem, storageLocation);
        movement.setUserId(authContextService.getCurrentUserId());

        var newMovement = movementRepository.save(movement);

        stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouse, storageLocation,
                movementDtoReceipt.getQuantity(), movementDtoReceipt.getMovementType());

        var newMovementDto = ReceiptMapper.mapToDto(newMovement);
        setFieldsToDto(newMovementDto, batch, serialItem);

        log.info("Оформили приход с id и номером документа {} и {}", newMovementDto.getId(), newMovementDto.getNumber());

        return newMovementDto;
    }

    private BatchDto saveBatch(BatchDto batch, NomenclatureDto nomenclature, CounterpartyDto supplier) {
        if (nomenclature.getInventoryType() == InventoryType.BATCH &&
                (batch == null || batch.getExpiryDate() == null || batch.getProductionDate() == null)) {
            log.warn("Не указаны данные партии для номенклатуры с id {}", nomenclature.getId());
            throw new ValidationException("Для номенклатуры с партионным учётом нужно указать данные о партии");
        }

        if (batch != null) {
            batch.setNomenclatureId(nomenclature.getId());
            batch.setSupplierId(supplier.getId());
            batch = batchService.create(batch);

            return batch;
        } else return null;
    }

    private SerialItemDto saveSerialItem(SerialItemDto serialItem, BatchDto batch, NomenclatureDto nomenclature) {
        if (nomenclature.getInventoryType() == InventoryType.SERIAL &&
                (serialItem == null || serialItem.getPassportNumber() == null || serialItem.getSerialNumber() == null)) {
            log.warn("Не указаны данные серийника для номенклатуры с id {}", nomenclature.getId());
            throw new ValidationException("Для серийных единиц нужно указать данные об оборудовании");
        }
        if (serialItem != null) {
            if (batch != null) {
                serialItem.setBatchId(batch.getId());
            }
            serialItem.setStatus(ItemStatus.IN_STOCK);
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
        movement.setStorageLocation(StorageLocationMapper.mapToEntityWithId(storageLocation));
        if (batch != null) {
            movement.setBatch(BatchMapper.mapToEntityWithId(batch));
        }
        if (serialItem != null) {
            movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
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

    private void checkSerialItemQuantity(MovementDtoReceipt movementDtoReceipt, NomenclatureDto nomenclatureDto) {
        if (nomenclatureDto.getInventoryType() == InventoryType.SERIAL &&
                !movementDtoReceipt.getQuantity().equals(BigDecimal.ONE)) {
            log.warn("Неверно введено количество серийный единиц, приемка идет по 1");
            throw new ValidationException("Для номенклатуры с серийным учётом количество должно быть равно 1");
        }
    }

}
