package com.example.warehouseApp.controller.movement.writeOff;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.movement.MovementDtoWriteOff;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.movement.WriteOffService;
import com.example.warehouseApp.service.serialItem.SerialItemService;
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
import java.util.Collections;

@Controller
@RequestMapping("/movements/write-off")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
public class WriteOffController {

    private final WriteOffService writeOffService;
    private final StorageLocationService storageLocationService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BatchService batchService;
    private final SerialItemService serialItemService;

    @GetMapping("/form")
    public String writeOffForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long warehouseFromId,
            @RequestParam(required = false) String warehouseFromName,
            @RequestParam(required = false) Long storageLocationId,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));

        model.addAttribute("warehouses", warehouseService.getAll(pageable, null).getContent());
        model.addAttribute("selectedNomenclatureId", nomenclatureId);
        model.addAttribute("selectedNomenclatureName", nomenclatureName);
        model.addAttribute("selectedWarehouseFromId", warehouseFromId);
        model.addAttribute("selectedWarehouseFromName", warehouseFromName);
        model.addAttribute("selectedStorageLocationId", storageLocationId);
        model.addAttribute("selectedInventoryType", inventoryType);
        model.addAttribute("fromOperation", fromOperation);

        boolean isBatchRequired = false;
        if (inventoryType != null) {
            isBatchRequired = inventoryType.equals(InventoryType.BATCH.toString());
        }
        model.addAttribute("isBatchRequired", isBatchRequired);

        boolean isSerialRequired = false;
        if (inventoryType != null) {
            isSerialRequired = inventoryType.equals(InventoryType.SERIAL.toString());
        }
        model.addAttribute("isSerialRequired", isSerialRequired);

        if (warehouseFromId != null && nomenclatureId != null) {
            var locations = storageLocationService.getLocationsWithStock(nomenclatureId, warehouseFromId);
            model.addAttribute("storageLocations", locations);
        } else {
            model.addAttribute("storageLocations", Collections.emptyList());
        }

        if (warehouseFromId != null && nomenclatureId != null) {
            var batches = batchService.getBatchesWithStockOnWarehouse(nomenclatureId, warehouseFromId);
            model.addAttribute("batchesWithStock", batches);
        } else {
            model.addAttribute("batchesWithStock", Collections.emptyList());
        }

        if (isSerialRequired && warehouseFromId != null && nomenclatureId != null) {
            var serialItems = serialItemService.findByNomenclatureAndWarehouseWithStock(
                    nomenclatureId,
                    warehouseFromId,
                    ItemStatus.IN_STOCK
            );
            model.addAttribute("serialItemsWithStock", serialItems);
        } else {
            model.addAttribute("serialItemsWithStock", Collections.emptyList());
        }

        return "movements/write-off-form";
    }

    @PostMapping
    public String processWriteOff(
            @RequestParam("nomenclatureId") Long nomenclatureId,
            @RequestParam("warehouseFrom") Long warehouseFrom,
            @RequestParam("storageLocationId") Long storageLocationId,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long serialItemId,
            @RequestParam("notes") String notes,
            RedirectAttributes redirectAttributes) {

        try {
            var dto = new MovementDtoWriteOff();
            dto.setMovementType(MovementType.WRITE_OFF);
            dto.setNomenclatureId(nomenclatureId);
            dto.setWarehouseFrom(warehouseFrom);
            dto.setStorageLocationId(storageLocationId);
            dto.setQuantity(quantity);
            dto.setNotes(notes);

            if (batchId != null) {
                dto.setBatchId(batchId);
            }
            if (serialItemId != null) {
                dto.setSerialItemId(serialItemId);
            }

            var result = writeOffService.createWriteOff(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Списание оформлено. Номер документа: " + result.getNumber());
            return "redirect:/movements";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении списания: " + e.getMessage());
            String redirectUrl = "/movements/write-off/form";
            if (nomenclatureId != null) redirectUrl += "?nomenclatureId=" + nomenclatureId;
            if (warehouseFrom != null)
                redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "warehouseFrom=" + warehouseFrom;
            return "redirect:" + redirectUrl;
        }
    }
}
