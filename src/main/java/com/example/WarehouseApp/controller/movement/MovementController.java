package com.example.WarehouseApp.controller.movement;

import com.example.WarehouseApp.dto.*;
import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.movement.IssueService;
import com.example.WarehouseApp.service.movement.ReceiptService;
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

@Controller
@RequestMapping("/movements")
@RequiredArgsConstructor
@Slf4j
public class MovementController {

    private final ReceiptService receiptService;
    private final IssueService issueService;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;

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
                serialItemDto.setStatus(ItemStatus.IN_STOCK);
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
            @RequestParam(required = false) String fromOperation, // <-- Это ключевой параметр
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("id"));
        var result = warehouseService.getAll(pageable, name != null && !name.isBlank() ? "name:" + name : null);

        model.addAttribute("warehouses", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("filterName", name != null ? name : "");

        // Пробрасываем все параметры, чтобы они не потерялись при пагинации
        model.addAttribute("nomenclatureId", nomenclatureId);
        model.addAttribute("nomenclatureName", nomenclatureName);
        model.addAttribute("counterpartyId", counterpartyId);
        model.addAttribute("counterpartyName", counterpartyName);
        model.addAttribute("inventoryType", inventoryType);
        model.addAttribute("fromOperation", fromOperation); // <-- Сохраняем контекст

        model.addAttribute("searchPerformed", name != null && !name.isBlank());

        return "movements/warehouse-list";
    }

    @GetMapping("/issue/form")
    public String issueForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long customerId,          // для ISSUE это клиент/получатель
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Long warehouseFrom,       // это и есть warehouseId
            @RequestParam(required = false) String warehouseName,     // это и есть warehouseName
            @RequestParam(required = false) String inventoryType,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));
        model.addAttribute("storageLocations", storageLocationService.getAll(pageable, null).getContent());

        // Номенклатура
        if (nomenclatureId != null) {
            model.addAttribute("selectedNomenclatureId", nomenclatureId);
            model.addAttribute("selectedNomenclatureName", nomenclatureName);
        }

        // Клиент/получатель (для расхода)
        if (customerId != null) {
            model.addAttribute("selectedCustomerId", customerId);
            model.addAttribute("selectedCustomerName", customerName);
        }

        // Склад (откуда) — вот тут и кроется проблема, если не подставляется
        if (warehouseFrom != null) {
            model.addAttribute("selectedWarehouseFromId", warehouseFrom);
            model.addAttribute("selectedWarehouseFromName", warehouseName);
        }

        // Тип учёта
        if (inventoryType != null && !inventoryType.isBlank()) {
            model.addAttribute("selectedInventoryType", inventoryType);
        }

        return "movements/issue-form";
    }
    @PostMapping("/issue")
    public String processIssue(
            @RequestParam("nomenclatureId") Long nomenclatureId,
            @RequestParam("customerId") Long customerId,
            @RequestParam("warehouseFrom") Long warehouseFrom,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("price") BigDecimal price,
            @RequestParam("userId") Long userId,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        // Валидация
        if (nomenclatureId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Выберите номенклатуру");
            return "redirect:/movements/issue/form";
        }
        if (customerId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Выберите получателя (клиента)");
            return "redirect:/movements/issue/form";
        }
        if (warehouseFrom == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Выберите склад (откуда)");
            return "redirect:/movements/issue/form";
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Количество должно быть больше 0");
            return "redirect:/movements/issue/form";
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Цена не может быть отрицательной");
            return "redirect:/movements/issue/form";
        }
        if (userId == null || userId <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Укажите корректного сотрудника");
            return "redirect:/movements/issue/form";
        }

        try {
            MovementDtoIssue dto = new MovementDtoIssue();
            dto.setMovementType(MovementType.ISSUE);
            dto.setNomenclatureId(nomenclatureId);
            dto.setWarehouseFrom(warehouseFrom);
            dto.setCustomerId(customerId);
            dto.setQuantity(quantity);
            dto.setPrice(price);
            dto.setUserId(userId);

            if (notes != null && !notes.isBlank()) {
                dto.setNotes(notes);
            }

            // Если у тебя в DTO есть поле inventoryType — можно положить его сюда,
            // но для POST-запроса оно не обязательно.
            // String inventoryType = request.getParameter("inventoryType");

            var result = issueService.createIssue(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Отгрузка оформлена. Номер документа: " + result.getNumber());
            return "redirect:/movements";

        } catch (Exception e) {
            e.printStackTrace(); // Теперь ошибка точно будет в консоли
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении отгрузки: " + e.getMessage());
            return "redirect:/movements/issue/form";
        }
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
