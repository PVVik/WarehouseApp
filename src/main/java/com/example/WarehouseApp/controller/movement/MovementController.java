package com.example.WarehouseApp.controller.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.movement.ReceiptService;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    private final ReceiptService receiptService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;

    @GetMapping
    public String showActions() {
        return "movements/movements-actions";
    }

    @GetMapping("/issue/stub")
    public String issueStub(Model model) {
        model.addAttribute("message", "Расход — функционал в разработке");
        return "stub";
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

    @GetMapping("/receipt/form")
    public String receiptForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String counterpartyName,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) String inventoryType,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));
        model.addAttribute("storageLocations", storageLocationService.getAll(pageable, null).getContent());

        if (nomenclatureId != null) {
            model.addAttribute("selectedNomenclatureId", nomenclatureId);
            model.addAttribute("selectedNomenclatureName", nomenclatureName);
        }
        if (counterpartyId != null) {
            model.addAttribute("selectedCounterpartyId", counterpartyId);
            model.addAttribute("selectedCounterpartyName", counterpartyName);
        }
        if (warehouseId != null) {
            model.addAttribute("selectedWarehouseId", warehouseId);
            model.addAttribute("selectedWarehouseName", warehouseName);
        }

        if (inventoryType != null && !inventoryType.isBlank()) {
            model.addAttribute("selectedInventoryType", inventoryType);
        }

        return "movements/receipt-form";
    }

    @PostMapping("/receipt")
    public String processReceipt(@RequestParam(required = false) Long nomenclatureId,
                                 @RequestParam(required = false) String inventoryType,
                                 @RequestParam(required = false) Long counterpartyId,
                                 @RequestParam(required = false) Long warehouseTo,
                                 @RequestParam(required = false) Long storageLocationId,
                                 @RequestParam(required = false) BigDecimal quantity,
                                 @RequestParam(required = false) BigDecimal price,
                                 @RequestParam(required = false) Long userId,
                                 @RequestParam(required = false) String notes,
                                 @RequestParam(required = false) String batchNumber,
                                 @RequestParam(required = false) String batchManufactureDate,
                                 @RequestParam(required = false) String batchExpiryDate,
                                 @RequestParam(required = false) String serialNumber,
                                 @RequestParam(required = false) String passportNumber,
                                 @RequestParam(required = false) String serialNotes,
                                 RedirectAttributes redirectAttributes) {

        if (nomenclatureId == null || counterpartyId == null || warehouseTo == null
                || quantity == null || price == null || userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Заполните все обязательные поля: номенклатура, поставщик, склад, количество, цена и ID сотрудника.");
            return "redirect:/movements/receipt/form";
        }

        var contextQuery = buildContextQuery(nomenclatureId, null,
                inventoryType, counterpartyId, warehouseTo);

        boolean requiresBatch = "BATCH".equals(inventoryType);
        boolean requiresSerial = "SERIAL".equals(inventoryType);

        if (requiresBatch && (batchNumber == null || batchNumber.isBlank())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Для номенклатуры с партионным учётом нужно указать номер партии.");
            return "redirect:/movements/receipt/form" + contextQuery;
        }
        if (requiresSerial && (serialNumber == null || serialNumber.isBlank())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Для номенклатуры с серийным учётом нужно указать серийный номер.");
            return "redirect:/movements/receipt/form" + contextQuery;
        }
        if (requiresSerial && (passportNumber == null || passportNumber.isBlank())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Для номенклатуры с серийным учётом нужно указать номер паспорта.");
            return "redirect:/movements/receipt/form" + contextQuery;
        }
        if (requiresSerial && quantity != null && quantity.compareTo(BigDecimal.ONE) != 0) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Для номенклатуры с серийным учётом количество должно быть равно 1.");
            return "redirect:/movements/receipt/form" + contextQuery;
        }

        try {
            var dto = new MovementDtoReceipt();
            dto.setMovementType(MovementType.RECEIPT);
            dto.setNomenclatureId(nomenclatureId);
            dto.setCounterpartyId(counterpartyId);
            dto.setWarehouseTo(warehouseTo);
            dto.setStorageLocationId(storageLocationId);
            dto.setQuantity(quantity);
            dto.setPrice(price);
            dto.setUserId(userId);
            if (notes != null && !notes.isBlank()) {
                dto.setNotes(notes);
            }

            if (batchNumber != null && !batchNumber.isBlank()) {
                var batchDto = new BatchDto();
                batchDto.setBatchNumber(batchNumber);
                if (batchManufactureDate != null && !batchManufactureDate.isBlank()) {
                    batchDto.setProductionDate(LocalDate.parse(batchManufactureDate));
                }
                if (batchExpiryDate != null && !batchExpiryDate.isBlank()) {
                    batchDto.setExpiryDate(LocalDate.parse(batchExpiryDate));
                }
                dto.setBatchDto(batchDto);
            }

            if (serialNumber != null && !serialNumber.isBlank()) {
                var serialItemDto = new SerialItemDto();
                serialItemDto.setSerialNumber(serialNumber);
                serialItemDto.setPassportNumber(passportNumber);
                serialItemDto.setStatus(ItemStatus.NEW);
                if (serialNotes != null && !serialNotes.isBlank()) {
                    serialItemDto.setNotes(serialNotes);
                }
                dto.setSerialItemDto(serialItemDto);
            }

            var result = receiptService.createReceipt(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Приход оформлен. Номер документа: " + result.getNumber());
            return "redirect:/movements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении прихода: " + e.getMessage());
            return "redirect:/movements/receipt/form" + contextQuery;
        }
    }

    @GetMapping("/nomenclature/list")
    public String nomenclatureSearchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var result = nomenclatureService.getAll(pageable, name != null ? "name:" + name : null);

        model.addAttribute("nomenclatureList", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("filterName", name != null ? name : "");

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

        model.addAttribute("searchPerformed", name != null && !name.isBlank());

        return "movements/warehouse-list";
    }

    private String buildContextQuery(Long nomenclatureId, String nomenclatureName,
                                     String inventoryType, Long counterpartyId, Long warehouseTo) {
        var sb = new StringBuilder("?");
        if (nomenclatureId != null) sb.append("nomenclatureId=").append(nomenclatureId).append("&");
        if (nomenclatureName != null && !nomenclatureName.isBlank())
            sb.append("nomenclatureName=").append(URLEncoder.encode(nomenclatureName, StandardCharsets.UTF_8)).append("&");
        if (inventoryType != null && !inventoryType.isBlank())
            sb.append("inventoryType=").append(inventoryType).append("&");
        if (counterpartyId != null) sb.append("counterpartyId=").append(counterpartyId).append("&");
        if (warehouseTo != null) sb.append("warehouseId=").append(warehouseTo);
        return sb.toString();
    }

}
