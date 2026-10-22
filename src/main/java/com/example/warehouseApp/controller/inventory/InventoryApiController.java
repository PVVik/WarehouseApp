package com.example.warehouseApp.controller.inventory;

import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryService inventoryService;

    @PostMapping("/checks")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryCheckDtoResponse createCheck(@RequestParam Long warehouseId) {
        return inventoryService.createCheck(warehouseId);
    }

    @PutMapping("/lines/{lineId}/actual-qty")
    public InventoryLineDto updateActualQty(@PathVariable Long lineId, @RequestParam BigDecimal qty) {
        inventoryService.updateActualQty(lineId, qty);

        return inventoryService.getLineById(lineId);
    }

    @PostMapping("/checks/{checkId}/complete")
    public InventoryDiscrepancyActDto completeCheck(@PathVariable Long checkId) {
        return inventoryService.completeCheck(checkId);
    }

    @GetMapping("/checks")
    public List<InventoryCheckDtoResponse> getAllChecks() {
        return inventoryService.getAllChecks();
    }

    @GetMapping("/checks/{id}")
    public InventoryCheckDtoResponse getCheckById(@PathVariable Long id) {
        return inventoryService.getById(id);
    }

    @GetMapping("/checks/{checkId}/lines")
    public List<InventoryLineDto> getLinesByCheckId(@PathVariable Long checkId) {
        return inventoryService.getLinesByCheckId(checkId);
    }
}
