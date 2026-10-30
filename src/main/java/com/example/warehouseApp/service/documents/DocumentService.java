package com.example.warehouseApp.service.documents;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.documents.ReceiptDocumentResultDto;
import com.example.warehouseApp.dto.documents.ReceiptLineBreakdownDto;
import com.example.warehouseApp.dto.documents.WriteOffDocumentResultDto;
import com.example.warehouseApp.dto.documents.WriteOffLineBreakdownDto;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.model.*;
import com.example.warehouseApp.repository.InventoryDiscrepancyRepository;
import com.example.warehouseApp.repository.ReceiptDocumentRepository;
import com.example.warehouseApp.repository.WriteOffDocumentRepository;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.domain.AuthContextService;
import com.example.warehouseApp.service.nomenclature.NomenclatureService;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import com.example.warehouseApp.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private final StockBalanceService stockBalanceService;
    private final AuthContextService authContextService;
    private final WriteOffDocumentRepository writeOffRepo;
    private final BatchService batchService;
    private final NomenclatureService nomenclatureService;
    private final WarehouseService warehouseService;
    private final InventoryDiscrepancyRepository inventoryDiscrepancyRepository;
    private final ReceiptDocumentRepository receiptDocumentRepository;

    @Transactional
    public WriteOffDocumentResultDto completeWriteOffByActId(Long actId, String reason) {
        log.info("Начало проведения списаний по акту: actId={}", actId);

        var currentUserId = authContextService.getCurrentUserId();
        var act = inventoryDiscrepancyRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Акт с ID " + actId + " не найден"));

        WriteOffDocument optDoc = writeOffRepo.findByAct(act).orElseThrow(() ->
                new NotFoundException(String.format("Не найден документ на списание у акта %d", actId)));

        return processSingleWriteOff(optDoc, currentUserId, reason);
    }

    @Transactional
    public ReceiptDocumentResultDto completeReceiptByActId(Long actId, String source) {
        log.info("Начало проведения оприходования по акту: actId={}", actId);

        var currentUserId = authContextService.getCurrentUserId();
        var act = inventoryDiscrepancyRepository.findById(actId)
                .orElseThrow(() -> new NotFoundException("Акт с ID " + actId + " не найден"));

        ReceiptDocument doc = receiptDocumentRepository.findByAct(act)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Не найден документ на оприходование у акта %d", actId)));

        return processSingleReceipt(doc, currentUserId, source);
    }

    private ReceiptDocumentResultDto processSingleReceipt(ReceiptDocument doc, Long currentUserId, String source) {
        if (!DocumentStatus.DRAFT.equals(doc.getStatus())) {
            throw new IllegalStateException(
                    "Документ " + doc.getId() + " не в статусе DRAFT. Текущий статус: " + doc.getStatus());
        }

        var act = doc.getAct();
        if (act == null) {
            throw new IllegalStateException("У документа " + doc.getId() + " нет связанного акта");
        }

        var check = act.getCheck();
        var warehouse = (check != null) ? check.getWarehouse() : null;
        if (warehouse == null) {
            throw new IllegalStateException("У акта нет склада");
        }
        Long warehouseId = warehouse.getId();

        LocalDate now = LocalDate.now();

        List<ReceiptLineBreakdownDto> breakdown = new ArrayList<>();
        applyReceiptAllLines(act, breakdown, warehouseId);

        doc.setSource(source);
        doc.setCompletedBy(currentUserId);
        doc.setUpdatedAt(now);
        doc.setStatus(DocumentStatus.COMPLETED);

        receiptDocumentRepository.save(doc);

        return new ReceiptDocumentResultDto(
                doc.getId(),
                doc.getStatus(),
                doc.getSource(),
                doc.getUpdatedAt(),
                doc.getCompletedBy(),
                breakdown
        );
    }

    private void applyReceiptAllLines(InventoryDiscrepancyAct act,
                                      List<ReceiptLineBreakdownDto> breakdown,
                                      Long warehouseId) {

        if (act.getLines() == null) {
            throw new IllegalStateException("В акте отсутствуют строки для оприходования");
        }

        var lines = act.getLines().stream()
                .filter(l -> l.getType() == DiscrepancyType.RECEIPT)
                .toList();

        if (lines.isEmpty()) {
            throw new IllegalStateException("В акте нет строк типа RECEIPT для оприходования");
        }

        WarehouseDto warehouseDto = warehouseService.getById(warehouseId);

        for (DiscrepancyLine line : lines) {
            BigDecimal qty = line.getDifference().abs();
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Nomenclature nomenclature = line.getNomenclature();
            if (nomenclature == null) {
                throw new IllegalStateException("В строке акта отсутствует номенклатура");
            }
            NomenclatureDto nomenclatureDto = nomenclatureService.getById(nomenclature.getId());

            BatchDto batch = null;
            if (line.getBatch() != null) {
                batch = batchService.getById(line.getBatch().getId());
                if (batch == null) {
                    throw new NotFoundException("Партия не найдена: " + line.getBatch().getId());
                }
            }

            StockBalance sb = stockBalanceService.saveStockBalance(
                    nomenclatureDto,
                    batch,
                    null,
                    warehouseDto,
                    null,
                    qty,
                    MovementType.RECEIPT
            );

            breakdown.add(new ReceiptLineBreakdownDto(
                    nomenclatureDto.getId(),
                    nomenclatureDto.getName(),
                    sb.getBatch() != null ? sb.getBatch().getId() : null,
                    sb.getBatch() != null ? sb.getBatch().getBatchNumber() : null,
                    sb.getSerialItem() != null ? sb.getSerialItem().getId() : null,
                    sb.getSerialItem() != null ? sb.getSerialItem().getSerialNumber() : null,
                    qty
            ));
        }
    }

    private WriteOffDocumentResultDto processSingleWriteOff(WriteOffDocument doc, Long currentUserId, String reason) {
        if (!DocumentStatus.DRAFT.equals(doc.getStatus())) {
            throw new IllegalStateException(
                    "Документ " + doc.getId() + " не в статусе DRAFT. Текущий статус: " + doc.getStatus());
        }

        var act = doc.getAct();
        if (act == null) {
            throw new IllegalStateException("У документа " + doc.getId() + " нет связанного акта");
        }

        var check = act.getCheck();
        var warehouse = (check != null) ? check.getWarehouse() : null;
        if (warehouse == null) {
            throw new IllegalStateException("У акта нет склада");
        }
        Long warehouseId = warehouse.getId();

        LocalDate now = LocalDate.now();
        doc.setReason(reason);
        doc.setCompletedBy(currentUserId);
        doc.setUpdatedAt(now);
        doc.setStatus(DocumentStatus.COMPLETED);

        List<WriteOffLineBreakdownDto> breakdown = new ArrayList<>();

        applyWriteOffAllLines(act, breakdown, warehouseId);

        writeOffRepo.save(doc);

        return new WriteOffDocumentResultDto(
                doc.getId(), doc.getStatus(),
                // quantity убран
                doc.getReason(),
                doc.getUpdatedAt(),
                doc.getCompletedBy(),
                breakdown
        );
    }

    private void applyWriteOffAllLines(InventoryDiscrepancyAct act,
                                       List<WriteOffLineBreakdownDto> breakdown,
                                       Long warehouseId) {

        if (act.getLines() == null) {
            throw new IllegalStateException("В акте отсутствуют строки для списания");
        }

        var lines = act.getLines().stream()
                .filter(l -> l.getType() == DiscrepancyType.WRITE_OFF)
                .toList();

        if (lines.isEmpty()) {
            throw new IllegalStateException("В акте нет строк типа WRITE_OFF для списания");
        }

        for (DiscrepancyLine line : lines) {
            BigDecimal qty = line.getDifference().compareTo(BigDecimal.ZERO) > 0
                    ? line.getDifference()
                    : BigDecimal.ZERO;

            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Nomenclature nomenclature = line.getNomenclature();
            if (nomenclature == null) {
                throw new IllegalStateException("В строке акта отсутствует номенклатура");
            }

            if (line.getBatch() != null) {
                var batch = batchService.getById(line.getBatch().getId());
                if (batch == null) {
                    throw new NotFoundException("Партия не найдена: " + line.getBatch().getId());
                }
                applyWriteOffByBatch(batch, qty, nomenclature, breakdown, warehouseId);
                continue;
            }

            applyWriteOffFEFO(nomenclature, qty, breakdown, warehouseId);
        }
    }

    private void applyWriteOffByBatch(BatchDto batch, BigDecimal qty, Nomenclature nomenclature,
                                      List<WriteOffLineBreakdownDto> breakdown, Long warehouseId) {

        NomenclatureDto nomenclatureDto = nomenclatureService.getById(nomenclature.getId());
        WarehouseDto warehouseDto = warehouseService.getById(warehouseId);

        StockBalance sb = stockBalanceService.saveStockBalance(
                nomenclatureDto, batch, null, warehouseDto, null, qty, MovementType.WRITE_OFF
        );

        breakdown.add(new WriteOffLineBreakdownDto(
                nomenclatureDto.getId(), nomenclatureDto.getName(),
                sb.getBatch() != null ? sb.getBatch().getId() : null,
                sb.getBatch() != null ? sb.getBatch().getBatchNumber() : null,
                sb.getSerialItem() != null ? sb.getSerialItem().getId() : null,
                sb.getSerialItem() != null ? sb.getSerialItem().getSerialNumber() : null,
                qty
        ));
    }

    private void applyWriteOffFEFO(Nomenclature nomenclature, BigDecimal qty,
                                   List<WriteOffLineBreakdownDto> breakdown, Long warehouseId) {

        WarehouseDto warehouseDto = warehouseService.getById(warehouseId);
        NomenclatureDto nomenclatureDto = nomenclatureService.getById(nomenclature.getId());

        var allBalances = new ArrayList<>(stockBalanceService.findPositiveBalancesByWarehouse(warehouseId).stream()
                .filter(b -> b.getNomenclature().getId().equals(nomenclature.getId()))
                .toList());

        if (allBalances.isEmpty()) {
            throw new NotFoundException(
                    "Нет остатков по номенклатуре " + nomenclature.getName() + " на складе ID " + warehouseId
            );
        }

        allBalances.sort(Comparator.comparing(
                b -> b.getBatch() != null ? b.getBatch().getExpiryDate() : null,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        BigDecimal left = qty;
        for (StockBalance balance : allBalances) {
            if (left.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal available = balance.getQuantity();
            BigDecimal take = available.min(left);

            StockBalance sb = stockBalanceService.saveStockBalance(
                    nomenclatureDto, null, null, warehouseDto, null, take, MovementType.WRITE_OFF
            );

            breakdown.add(new WriteOffLineBreakdownDto(
                    nomenclatureDto.getId(), nomenclatureDto.getName(),
                    sb.getBatch() != null ? sb.getBatch().getId() : null,
                    sb.getBatch() != null ? sb.getBatch().getBatchNumber() : null,
                    sb.getSerialItem() != null ? sb.getSerialItem().getId() : null,
                    sb.getSerialItem() != null ? sb.getSerialItem().getSerialNumber() : null,
                    take
            ));
            left = left.subtract(take);
        }

        if (left.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Не хватило остатков для полного списания по номенклатуре " + nomenclature.getName());
        }
    }
}
