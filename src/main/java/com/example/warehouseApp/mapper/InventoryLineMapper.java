package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.model.InventoryLine;

public class InventoryLineMapper {

    public static InventoryLineDto mapToDto(InventoryLine inventoryLine) {
        InventoryLineDto dto = new InventoryLineDto(inventoryLine.getId(), inventoryLine.getNomenclature().getName(),
                inventoryLine.getAccountedQty(), inventoryLine.getActualQty());
        if (inventoryLine.getBatch() != null) {
            dto.setBatchNumber(inventoryLine.getBatch().getBatchNumber());
        }

        return dto;
    }
}
