package com.example.warehouseApp.service.inventory;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.mapper.InventoryCheckMapper;
import com.example.warehouseApp.mapper.InventoryDiscrepancyActMapper;
import com.example.warehouseApp.mapper.InventoryLineMapper;
import com.example.warehouseApp.mapper.WarehouseMapper;
import com.example.warehouseApp.model.*;
import com.example.warehouseApp.repository.*;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.domain.AuthContextService;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryCheckRepository inventoryCheckRepository;
    private final InventoryLineRepository inventoryLineRepository;
    private final BaseService<WarehouseDto> warehouseService;
    private final AuthContextService authContextService;
    private final StockBalanceService stockBalanceService;
    private final InventoryDiscrepancyRepository inventoryDiscrepancyRepository;
    private final DiscrepancyLineRepository discrepancyLineRepository;
    private final WriteOffDocumentRepository writeOffDocumentRepository;
    private final ReceiptDocumentRepository receiptDocumentRepository;

    @Transactional
    @LogAudit(action = "CREATE", entityType = InventoryCheck.class)
    public InventoryCheckDtoResponse createCheck(Long warehouseId) {
        WarehouseDto warehouse = warehouseService.getById(warehouseId);

        InventoryCheck inventoryCheck = createInventoryCheckEntity(warehouse);
        InventoryCheck savedCheck = inventoryCheckRepository.save(inventoryCheck);
        log.info("Создали черновик инвентаризации с id {} для склада {}", savedCheck.getId(), warehouse.getId());

        List<StockBalance> stockBalances = stockBalanceService.findPositiveBalancesByWarehouse(warehouse.getId());
        if (stockBalances.isEmpty()) {
            log.info("Нет остатков на складе {}", warehouse.getId());
            InventoryCheckDtoResponse dto = InventoryCheckMapper.mapToDto(savedCheck);
            dto.setTotalLines(0L);
            return dto;
        }

        List<InventoryLine> inventoryLines = createInventoryLines(savedCheck, stockBalances);
        inventoryLineRepository.saveAll(inventoryLines);
        log.info("Для проверки создано {} строк", inventoryLines.size());

        InventoryCheckDtoResponse dto = InventoryCheckMapper.mapToDto(savedCheck);
        dto.setTotalLines((long) inventoryLines.size());

        return dto;
    }

    @Transactional
    @LogAudit(action = "UPDATE", entityType = InventoryCheck.class)
    public void updateActualQty(Long lineId, BigDecimal actualQty) {
        if (actualQty == null || actualQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("actualQty не может быть null или отрицательным");
        }

        InventoryLine line = inventoryLineRepository.findById(lineId)
                .orElseThrow(() -> new NotFoundException(String.format("Строка инвентаризации с id %d не найдена", lineId)));

        InventoryCheck check = inventoryCheckRepository.getReferenceById(line.getCheck().getId());
        if (check.getStatus() == DocumentStatus.COMPLETED) {
            throw new BusinessException("Нельзя менять фактические количества в завершённой инвентаризации");
        }

        line.setActualQty(actualQty);
        BigDecimal difference = actualQty.subtract(line.getAccountedQty());
        line.setDifference(difference);

        InventoryCheck inventoryCheck = inventoryCheckRepository.getReferenceById(line.getCheck().getId());
        inventoryCheck.setStatus(DocumentStatus.IN_PROGRESS);
        inventoryCheckRepository.save(inventoryCheck);

        inventoryLineRepository.save(line);
        log.info("Обновлено фактическое количество для строки {}: стало {}, расхождение {}", lineId, actualQty, difference);
    }

    @Transactional
    @LogAudit(action = "CREATE", entityType = InventoryDiscrepancyAct.class)
    public InventoryDiscrepancyActDto completeCheck(Long checkId) {
        InventoryCheck check = getAndValidateCheck(checkId);

        List<InventoryLine> lines = getAndValidateLines(checkId);
        markCheckAsCompleted(check);

        InventoryDiscrepancyAct act = createDiscrepancyAct(check);
        List<DiscrepancyLine> discrepancyLines = buildDiscrepancyLines(act, lines);

        inventoryLineRepository.saveAll(lines);
        inventoryDiscrepancyRepository.save(act);
        discrepancyLineRepository.saveAll(discrepancyLines);

        log.info("Инвентаризация {} завершена, создан акт расхождений {}", checkId, act.getId());

        return InventoryDiscrepancyActMapper.toDto(act);
    }

    public List<InventoryCheckDtoResponse> getAllChecks() {
        return inventoryCheckRepository.findAll().stream()
                .map(InventoryCheckMapper::mapToDto)
                .toList();
    }

    public InventoryCheckDtoResponse getById(Long id) {
        InventoryCheck check = inventoryCheckRepository.findById(id).orElseGet(() -> {
            log.warn("Не нашли инвентаризацию по id {}", id);
            throw new NotFoundException(String.format("Инвентаризации с id %d не существует", id));
        });

        return InventoryCheckMapper.mapToDto(check);
    }

    public List<InventoryLineDto> getLinesByCheckId(Long id) {
        return inventoryLineRepository.findAllByCheckId(id).stream().
                map(InventoryLineMapper::mapToDto)
                .toList();
    }

    public InventoryLineDto getLineById(Long lineId) {
        InventoryLine line = inventoryLineRepository.findById(lineId)
                .orElseGet(() -> {
                    log.info("Не нашли строку инвентаризации с id {}", lineId);
                    throw new NotFoundException(String.format("Строка инвентаризации с id %d не найдена", lineId));
                });
        return InventoryLineMapper.mapToDto(line);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @LogAudit(action = "UPDATE", entityType = InventoryDiscrepancyAct.class)
    public void settleDiscrepancies(Long actId) {
        InventoryDiscrepancyAct act = inventoryDiscrepancyRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Акт не найден"));

        if (act.getStatus() == DocumentStatus.COMPLETED) {
            return;
        }
        LocalDate now = LocalDate.now();

        for (DiscrepancyLine line : act.getLines()) {
            switch (line.getType()) {
                case WRITE_OFF:
                    WriteOffDocument wf = new WriteOffDocument();
                    wf.setAct(act);
                    wf.setQuantity(line.getDifference());
                    wf.setStatus(DocumentStatus.DRAFT);
                    wf.setCreatedAt(now);
                    wf.setUpdatedAt(now);
                    writeOffDocumentRepository.save(wf);
                    break;
                case RECEIPT:
                    ReceiptDocument rf = new ReceiptDocument();
                    rf.setAct(act);
                    rf.setQuantity(line.getDifference());
                    rf.setStatus(DocumentStatus.DRAFT);
                    rf.setCreatedAt(now);
                    rf.setUpdatedAt(now);
                    receiptDocumentRepository.save(rf);
                    break;
                default:
                    break;
            }
        }

        act.setStatus(DocumentStatus.COMPLETED);
        act.setApprovedAt(now);
        inventoryDiscrepancyRepository.save(act);

        log.info("Акт {} урегулирован. Созданы черновики документов.", actId);
    }

    public InventoryDiscrepancyActDto getActById(Long id) {
        InventoryDiscrepancyAct act = inventoryDiscrepancyRepository.findById(id).orElseGet(() -> {
            log.warn("Не наши акт по id {}", id);
            throw new NotFoundException(String.format("Акт по id %d не был найден", id));
        });

        log.info("Вернули акт по id {}", id);

        return InventoryDiscrepancyActMapper.toDto(act);
    }

    private InventoryCheck createInventoryCheckEntity(WarehouseDto warehouse) {
        Warehouse checkWarehouse = WarehouseMapper.mapToEntityWithId(warehouse);
        Long currentUserId = authContextService.getCurrentUserId();

        return new InventoryCheck(checkWarehouse, LocalDate.now(), DocumentStatus.DRAFT, currentUserId);
    }

    private List<InventoryLine> createInventoryLines(InventoryCheck check, List<StockBalance> stockBalances) {
        List<InventoryLine> lines = new ArrayList<>(stockBalances.size());

        for (StockBalance stockBalance : stockBalances) {
            InventoryLine line = new InventoryLine();
            line.setCheck(check);
            line.setNomenclature(stockBalance.getNomenclature());

            if (stockBalance.getBatch() != null) {
                line.setBatch(stockBalance.getBatch());
            }

            line.setAccountedQty(stockBalance.getQuantity());
            lines.add(line);
        }

        return lines;
    }

    private InventoryCheck getAndValidateCheck(Long checkId) {
        return inventoryCheckRepository.findById(checkId)
                .orElseThrow(() -> {
                    log.warn("Не найдена инвентаризация по id {}", checkId);
                    return new NotFoundException("Инвентаризация не найдена");
                });
    }

    private List<InventoryLine> getAndValidateLines(Long checkId) {
        List<InventoryLine> lines = inventoryLineRepository.findAllByCheckId(checkId);

        if (lines.isEmpty()) {
            log.warn("В инвентаризации {} нет строк", checkId);
            throw new BusinessException("Нет строк инвентаризации");
        }

        for (InventoryLine line : lines) {
            if (line.getActualQty() == null) {
                log.warn("В строке {} не введено фактическое количество", line.getId());
                throw new BusinessException(
                        "Есть строки без фактического количества: строка " + line.getId()
                );
            }
        }
        return lines;
    }

    private void markCheckAsCompleted(InventoryCheck check) {
        if (check.getStatus() == DocumentStatus.COMPLETED) {
            log.warn("Попытка завершить уже завершенную инвентаризацию {}", check.getId());
            throw new BusinessException("Инвентаризация уже завершена");
        }

        check.setStatus(DocumentStatus.COMPLETED);
        check.setCompletedAt(LocalDate.now());
        inventoryCheckRepository.save(check);
    }

    private InventoryDiscrepancyAct createDiscrepancyAct(InventoryCheck check) {
        InventoryDiscrepancyAct act = new InventoryDiscrepancyAct();
        act.setCreatedAt(LocalDate.now());
        act.setCheck(check);
        act.setCreatedBy(authContextService.getCurrentUserId());
        act.setStatus(DocumentStatus.DRAFT);
        return act;
    }

    private List<DiscrepancyLine> buildDiscrepancyLines(InventoryDiscrepancyAct act, List<InventoryLine> inventoryLines) {
        List<DiscrepancyLine> result = new ArrayList<>(inventoryLines.size());

        for (InventoryLine line : inventoryLines) {
            BigDecimal difference = line.getActualQty().subtract(line.getAccountedQty());
            line.setDifference(difference);

            DiscrepancyType type;
            BigDecimal amount;

            if (difference.compareTo(BigDecimal.ZERO) < 0) {
                type = DiscrepancyType.WRITE_OFF;
                amount = difference.abs();
            } else if (difference.compareTo(BigDecimal.ZERO) > 0) {
                type = DiscrepancyType.RECEIPT;
                amount = difference;
            } else {
                continue;
            }

            DiscrepancyLine dLine = new DiscrepancyLine();
            dLine.setAct(act);
            dLine.setNomenclature(line.getNomenclature());
            dLine.setBatch(line.getBatch());
            dLine.setAccountedQty(line.getAccountedQty());
            dLine.setActualQty(line.getActualQty());
            dLine.setDifference(amount);
            dLine.setType(type);

            result.add(dLine);
        }
        return result;
    }


}
