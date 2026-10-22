package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.model.InventoryCheck;

public class InventoryCheckMapper {

    public static InventoryCheckDtoResponse mapToDto(InventoryCheck inventoryCheck) {
        return new InventoryCheckDtoResponse(inventoryCheck.getId(), inventoryCheck.getWarehouse().getId(),
                inventoryCheck.getCheckDate(), inventoryCheck.getStatus());
    }
}
