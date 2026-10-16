package com.example.WarehouseApp.controller.movement.transfer;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.dto.movement.MovementDtoTransfer;
import com.example.WarehouseApp.model.InventoryType;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.service.batch.BatchService;
import com.example.WarehouseApp.service.movement.MovementTransferService;
import com.example.WarehouseApp.service.serialItem.SerialItemService;
import com.example.WarehouseApp.service.storageLocation.StorageLocationService;
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

@Controller
@RequestMapping("/movements/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final MovementTransferService transferService;
    private final StorageLocationService storageLocationService;
    private final BaseService<WarehouseDto> warehouseService;
    private final BatchService batchService;
    private final SerialItemService serialItemService;

    @GetMapping("/form")
    public String transferForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long warehouseFromId,
            @RequestParam(required = false) String warehouseFromName,
            @RequestParam(required = false) Long warehouseToId,
            @RequestParam(required = false) String warehouseToName,
            @RequestParam(required = false) Long storageLocationId,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));

        // Сохраняем всё, что уже выбрали
        model.addAttribute("warehouses", warehouseService.getAll(pageable, null).getContent());
        model.addAttribute("selectedNomenclatureId", nomenclatureId);
        model.addAttribute("selectedNomenclatureName", nomenclatureName);
        model.addAttribute("selectedWarehouseFromId", warehouseFromId);
        model.addAttribute("selectedWarehouseFromName", warehouseFromName);
        model.addAttribute("selectedWarehouseToId", warehouseToId);
        model.addAttribute("selectedWarehouseToName", warehouseToName);
        model.addAttribute("selectedStorageLocationId", storageLocationId);
        model.addAttribute("fromOperation", fromOperation);
        model.addAttribute("selectedInventoryType", inventoryType);

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
        }

        if (warehouseFromId != null && nomenclatureId != null) {
            var batches = batchService.getBatchesWithStockOnWarehouse(nomenclatureId, warehouseFromId);
            model.addAttribute("batchesWithStock", batches);
        }

        if (isSerialRequired && warehouseFromId != null && nomenclatureId != null) {
            var serialItems = serialItemService.findByNomenclatureAndWarehouseWithStock(
                    nomenclatureId,
                    warehouseFromId,
                    ItemStatus.IN_STOCK
            );
            model.addAttribute("serialItemsWithStock", serialItems);
        }


        return "movements/transfer-form";
    }

    @PostMapping
    public String processTransfer(
            @RequestParam("nomenclatureId") Long nomenclatureId,
            @RequestParam("warehouseFrom") Long warehouseFrom,
            @RequestParam("warehouseTo") Long warehouseTo,
            @RequestParam("storageLocationId") Long storageLocationId,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("userId") long userId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long serialItemId,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            var dto = new MovementDtoTransfer();
            dto.setMovementType(MovementType.TRANSFER);
            dto.setNomenclatureId(nomenclatureId);
            dto.setWarehouseFrom(warehouseFrom);
            dto.setWarehouseTo(warehouseTo);
            dto.setStorageLocationId(storageLocationId);
            dto.setQuantity(quantity);
            dto.setUserId(userId);

            if (batchId != null) {
                dto.setBatchId(batchId);
            }
            if (serialItemId != null) {
                dto.setSerialItemId(serialItemId);
            }
            if (notes != null && !notes.isBlank()) {
                dto.setNotes(notes);
            }

            var result = transferService.createTransfer(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Перемещение оформлено. Номер документа: " + result.getNumber());
            return "redirect:/movements";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении перемещения: " + e.getMessage());
            return "redirect:/movements/transfer/form";
        }
    }
}
