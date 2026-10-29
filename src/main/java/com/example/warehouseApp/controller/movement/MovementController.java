package com.example.warehouseApp.controller.movement;

import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.movement.IssueService;
import com.example.warehouseApp.service.movement.ReceiptService;
import com.example.warehouseApp.service.movement.TransferService;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final ReceiptService receiptService;
    private final IssueService issueService;
    private final BatchService batchService;
    private final SerialItemService serialItemService;
    private final TransferService transferService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public String showActions() {
        return "movements/movements-actions";
    }

    @GetMapping("/{id}/print-transfer-m13")
    @PreAuthorize("hasAnyRole('STOREKEEPER', 'ADMIN')")
    public String printTransferM13(@PathVariable Long id, Model model) {
        var dto = transferService.getById(id);
        log.info("DEBUG: batchId = {}", dto.getBatchId());

        var warehouseFromDto = warehouseService.getById(dto.getWarehouseFrom());
        var warehouseToDto = warehouseService.getById(dto.getWarehouseTo());
        var nomenclatureDto = nomenclatureService.getById(dto.getNomenclatureId());

        BatchDto batchDto = null;
        if (dto.getBatchId() != null) {
            batchDto = batchService.getById(dto.getBatchId());
            log.info(batchDto.toString());
        }

        SerialItemDto serialItemDto = null;
        if (dto.getSerialItemId() != null) {
            serialItemDto = serialItemService.getById(dto.getSerialItemId());
            log.info(serialItemDto.toString());
        }

        model.addAttribute("document", dto);
        model.addAttribute("warehouseFromName", warehouseFromDto.getName());
        model.addAttribute("warehouseToName", warehouseToDto.getName());
        model.addAttribute("nomenclatureName", nomenclatureDto.getName());
        model.addAttribute("unitName", nomenclatureDto.getUnitOfMeasure().getLabel());
        model.addAttribute("batchDto", batchDto);
        model.addAttribute("serialItemDto", serialItemDto);
        model.addAttribute("documentDateFormatted", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dto.getDate()));

        return "movements/transfer-m13-print";
    }

    @GetMapping("/{id}/print-m4")
    @PreAuthorize("hasAnyRole('STOREKEEPER', 'ADMIN')")
    public String printM4(@PathVariable Long id, Model model) {
        var dto = receiptService.getById(id);
        var warehouseDto = warehouseService.getById(dto.getWarehouseTo());
        var nomenclatureDto = nomenclatureService.getById(dto.getNomenclatureId());
        var counterpartyDto = counterpartyService.getById(dto.getCounterpartyId());

        model.addAttribute("document", dto);
        model.addAttribute("warehouseName", warehouseDto.getName());
        model.addAttribute("nomenclatureName", nomenclatureDto.getName());
        model.addAttribute("unitName", nomenclatureDto.getUnitOfMeasure().getLabel());
        model.addAttribute("counterpartyName", counterpartyDto.getName());
        model.addAttribute("documentDateFormatted", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dto.getDate()));
        return "movements/receipt-m4-print";
    }

    @GetMapping("/{id}/print-issue-m11")
    @PreAuthorize("hasAnyRole('STOREKEEPER', 'ADMIN')")
    public String printIssueM11(@PathVariable Long id, Model model) {
        var dto = issueService.getById(id);

        var warehouseDto = warehouseService.getById(dto.getWarehouseFrom());
        var nomenclatureDto = nomenclatureService.getById(dto.getNomenclatureId());
        var customerDto = counterpartyService.getById(dto.getCustomerId());

        BatchDto batchDto = null;
        if (dto.getBatchId() != null) {
            batchDto = batchService.getById(dto.getBatchId());
        }

        SerialItemDto serialItemDto = null;
        if (dto.getSerialItemId() != null) {
            serialItemDto = serialItemService.getById(dto.getSerialItemId());
        }

        model.addAttribute("document", dto);
        model.addAttribute("warehouseName", warehouseDto.getName());
        model.addAttribute("nomenclatureName", nomenclatureDto.getName());
        model.addAttribute("unitName", nomenclatureDto.getUnitOfMeasure().getLabel());
        model.addAttribute("customerName", customerDto.getName());
        model.addAttribute("batchDto", batchDto);
        model.addAttribute("serialItemDto", serialItemDto);
        model.addAttribute("documentDateFormatted", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dto.getDate()));

        return "movements/issue-m11-print";
    }

    @GetMapping("/nomenclature/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String nomenclatureSearchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var result = nomenclatureService.getAll(pageable, name != null ? "name:" + name : null);

        model.addAttribute("nomenclatureList", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("filterName", name != null ? name : "");
        model.addAttribute("fromOperation", fromOperation);

        return "movements/nomenclature-list";
    }

    @GetMapping("/counterparty/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String supplierSearchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("id"));
        var result = counterpartyService.getAll(pageable, name != null && !name.isBlank() ? "name:" + name : null);

        model.addAttribute("counterparties", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("filterName", name != null ? name : "");
        model.addAttribute("nomenclatureId", nomenclatureId);
        model.addAttribute("nomenclatureName", nomenclatureName);
        model.addAttribute("inventoryType", inventoryType);
        model.addAttribute("searchPerformed", name != null && !name.isBlank());
        model.addAttribute("fromOperation", fromOperation);

        return "movements/supplier-list";
    }

    @GetMapping("/warehouse/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String warehouseSearchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String counterpartyName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            @RequestParam(required = false) Long warehouseFromId,
            @RequestParam(required = false) String warehouseFromName,
            @RequestParam(required = false) Long storageLocationId,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("id"));
        var result = warehouseService.getAll(pageable, name != null && !name.isBlank() ? "name:" + name : null);

        model.addAttribute("warehouses", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("filterName", name != null ? name : "");
        model.addAttribute("nomenclatureId", nomenclatureId);
        model.addAttribute("nomenclatureName", nomenclatureName);
        model.addAttribute("counterpartyId", counterpartyId);
        model.addAttribute("counterpartyName", counterpartyName);
        model.addAttribute("inventoryType", inventoryType);
        model.addAttribute("fromOperation", fromOperation);
        model.addAttribute("warehouseFromId", warehouseFromId);
        model.addAttribute("warehouseFromName", warehouseFromName);
        model.addAttribute("storageLocationId", storageLocationId);
        model.addAttribute("searchPerformed", name != null && !name.isBlank());

        return "movements/warehouse-list";
    }

}
