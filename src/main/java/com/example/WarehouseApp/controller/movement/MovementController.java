package com.example.WarehouseApp.controller.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.movement.IssueService;
import com.example.WarehouseApp.service.movement.ReceiptService;
import com.example.WarehouseApp.service.storageLocation.StorageLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;

    @GetMapping
    public String showActions() {
        return "movements/movements-actions";
    }


    @GetMapping("/write-off/stub")
    public String writeOffStub(Model model) {
        model.addAttribute("message", "Списание — функционал в разработке");
        return "stub";
    }

    @GetMapping("/transfer/stub")
    public String transferStub(Model model) {
        model.addAttribute("message", "Между складами — функционал в разработке");
        return "stub";
    }

    @GetMapping("/nomenclature/list")
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
