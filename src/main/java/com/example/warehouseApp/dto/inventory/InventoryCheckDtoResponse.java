package com.example.warehouseApp.dto.inventory;

import com.example.warehouseApp.model.InventoryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InventoryCheckDtoResponse {

    private Long id;
    private Long warehouseId;
    private LocalDate checkDate;
    private InventoryStatus status;
    private Long totalLines;

    public InventoryCheckDtoResponse(Long id, Long warehouseId, LocalDate checkDate, InventoryStatus status) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.checkDate = checkDate;
        this.status = status;
    }
}
