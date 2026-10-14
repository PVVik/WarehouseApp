package com.example.WarehouseApp.controller.movement.issue;

import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.movement.IssueService;
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
import java.util.Collections;

@Controller
@RequestMapping("/movements/issue")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final StorageLocationService storageLocationService;

    @GetMapping("/form")
    public String issueForm(
            @RequestParam(required = false) Long nomenclatureId,
            @RequestParam(required = false) String nomenclatureName,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Long warehouseFromId,
            @RequestParam(required = false) String warehouseFromName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String fromOperation,
            Model model) {

        var pageable = PageRequest.of(0, 1000, Sort.by("id"));
        model.addAttribute("storageLocations", storageLocationService.getAll(pageable, null).getContent());
        model.addAttribute("selectedNomenclatureId", nomenclatureId);
        model.addAttribute("selectedNomenclatureName", nomenclatureName);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCustomerName", customerName);
        model.addAttribute("selectedWarehouseFromId", warehouseFromId);
        model.addAttribute("selectedWarehouseFromName", warehouseFromName);
        model.addAttribute("selectedInventoryType", inventoryType);
        model.addAttribute("fromOperation", fromOperation);

        if (warehouseFromId != null && nomenclatureId != null) {
            var locations = storageLocationService.getLocationsWithStock(nomenclatureId, warehouseFromId);
            model.addAttribute("storageLocations", locations);
        } else {
            model.addAttribute("storageLocations", Collections.emptyList());
        }

        return "movements/issue-form";
    }

    @PostMapping
    public String processIssue(
            @RequestParam("nomenclatureId") Long nomenclatureId,
            @RequestParam("customerId") Long customerId,
            @RequestParam("warehouseFrom") Long warehouseFrom,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("price") BigDecimal price,
            @RequestParam("userId") Long userId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long storageLocationId,
            RedirectAttributes redirectAttributes) {

        try {
            var dto = new MovementDtoIssue();
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

            if (storageLocationId != null) {
                dto.setStorageLocationId(storageLocationId);
            }

            var result = issueService.createIssue(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Отгрузка оформлена. Номер документа: " + result.getNumber());
            return "redirect:/movements";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении отгрузки: " + e.getMessage());
            return "redirect:/movements/issue/form";
        }
    }
}
