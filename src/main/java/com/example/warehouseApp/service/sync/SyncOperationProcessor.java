package com.example.warehouseApp.service.sync;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.sync.SyncOperationDto;
import com.example.warehouseApp.dto.sync.SyncResultItem;
import com.example.warehouseApp.mapper.BatchMapper;
import com.example.warehouseApp.mapper.NomenclatureMapper;
import com.example.warehouseApp.mapper.SerialItemMapper;
import com.example.warehouseApp.mapper.WarehouseMapper;
import com.example.warehouseApp.model.*;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.nomenclature.NomenclatureService;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import com.example.warehouseApp.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncOperationProcessor {

    private final MovementRepository movementRepository;
    private final NomenclatureService nomenclatureService;
    private final WarehouseService warehouseRepository;
    private final BatchService batchService;
    private final SerialItemService serialItemService;
    private final StockBalanceService stockBalanceService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @LogAudit(action = "CREATE", entityType = SyncResultItem.class)
    public SyncResultItem processOperation(int index, SyncOperationDto op) {
        try {
            NomenclatureDto nomenclature = nomenclatureService.getBySku(op.getNomenclatureSku());

            String cleanWarehouseName = op.getWarehouseName() != null ? op.getWarehouseName().trim() : null;
            if (cleanWarehouseName == null || cleanWarehouseName.isEmpty()) {
                return new SyncResultItem(index, false, "Не указано имя склада", null);
            }

            WarehouseDto warehouse = warehouseRepository.getByName(cleanWarehouseName);

            Movement movement = new Movement();
            movement.setMovementType(op.getType());
            movement.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
            movement.setDate(op.getDate());
            movement.setQuantity(op.getQuantity());
            movement.setNumber(op.getDocumentNumber());

            if (op.getType() == MovementType.ISSUE) {
                movement.setWarehouseFrom(WarehouseMapper.mapToEntityWithId(warehouse));
            } else {
                movement.setWarehouseTo(WarehouseMapper.mapToEntityWithId(warehouse));
            }

            BatchDto batch = resolveBatch(op, nomenclature);
            SerialItemDto serialItem = resolveSerialItem(op, nomenclature);

            if (batch != null) {
                movement.setBatch(BatchMapper.mapToEntityWithId(batch));
            }
            if (serialItem != null) {
                movement.setSerialItem(SerialItemMapper.mapToEntityWithId(serialItem));
            }

            stockBalanceService.saveStockBalance(nomenclature, batch, serialItem, warehouse, null, op.getQuantity(),
                    op.getType());

            Movement saved = movementRepository.save(movement);

            log.info("Синхронизировано движение №{}: тип={}, документ={}", index, op.getType(), op.getDocumentNumber());
            return new SyncResultItem(index, true, "Успешно", saved.getId());

        } catch (Exception e) {
            log.warn("Ошибка синхронизации операции №{}: {}", index, e.getMessage());
            return new SyncResultItem(index, false, e.getMessage(), null);
        }

    }

    private BatchDto resolveBatch(SyncOperationDto op, NomenclatureDto nomenclature) {
        if (op.getBatchNumber() == null || op.getBatchNumber().isBlank()) {
            return null;
        }

        Optional<Batch> existing = batchService.findByBatchNumberAndNomenclatureId(op.getBatchNumber(), nomenclature.getId());
        if (existing.isPresent()) {
            return BatchMapper.mapToDto(existing.get());
        }

        Batch newBatch = new Batch();
        newBatch.setStatus(BatchStatus.ACTIVE);
        newBatch.setBatchNumber(op.getBatchNumber());
        newBatch.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        newBatch.setProductionDate(op.getBatchProductionDate());
        newBatch.setExpiryDate(op.getBatchExpiryDate());

        return batchService.create(BatchMapper.mapToDto(newBatch));
    }

    private SerialItemDto resolveSerialItem(SyncOperationDto op, NomenclatureDto nomenclature) {
        if (op.getSerialNumber() == null || op.getSerialNumber().isBlank()) {
            return null;
        }

        if (!nomenclature.getInventoryType().equals(InventoryType.SERIAL)) {
            throw new IllegalArgumentException(String.format(
                    "Номенклатура '%s' не поддерживает серийный учёт, но передан серийник: %s",
                    nomenclature.getName(), op.getSerialNumber()));
        }

        Optional<SerialItem> existing = serialItemService.findBySerialNumber(op.getSerialNumber());
        if (existing.isPresent()) {
            return SerialItemMapper.mapToDto(existing.get());
        }

        SerialItem newSerialItem = new SerialItem();
        newSerialItem.setSerialNumber(op.getSerialNumber());
        newSerialItem.setPassportNumber(op.getPassportNumber());
        newSerialItem.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclature));
        newSerialItem.setStatus(ItemStatus.IN_STOCK);

        return serialItemService.create(SerialItemMapper.mapToDto(newSerialItem));
    }
}
