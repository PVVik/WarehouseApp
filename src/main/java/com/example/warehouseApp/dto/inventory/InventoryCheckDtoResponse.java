package com.example.warehouseApp.dto.inventory;

import com.example.warehouseApp.model.DocumentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InventoryCheckDtoResponse {

    private Long id;
    private Long warehouseId;
    private LocalDate checkDate;
    private DocumentStatus status;
    private Long totalLines;

    public InventoryCheckDtoResponse(Long id, Long warehouseId, LocalDate checkDate, DocumentStatus status) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.checkDate = checkDate;
        this.status = status;
    }
}
