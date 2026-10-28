package com.example.warehouseApp.controller.movement.receipt;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.service.movement.ReceiptService;
import com.example.warehouseApp.service.storageLocation.StorageLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/movements/receipt")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final StorageLocationService storageLocationService;

    @GetMapping("/form")
    public String receiptForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String counterpartyName,
            @RequestParam(required = false) Long warehouseTo,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));
        model.addAttribute("storageLocations", storageLocationService.getAll(pageable, null).getContent());
        model.addAttribute("selectedNomenclatureId", nomenclatureId);
        model.addAttribute("selectedNomenclatureName", nomenclatureName);
        model.addAttribute("selectedCounterpartyId", counterpartyId);
        model.addAttribute("selectedCounterpartyName", counterpartyName);
        model.addAttribute("selectedWarehouseId", warehouseTo);
        model.addAttribute("selectedWarehouseName", warehouseName);
        model.addAttribute("selectedInventoryType", inventoryType);
        model.addAttribute("fromOperation", fromOperation);

        return "movements/receipt-form";
    }

    @PostMapping
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

        var contextQuery = buildContextQuery(nomenclatureId, null,
                inventoryType, counterpartyId, warehouseTo);

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
