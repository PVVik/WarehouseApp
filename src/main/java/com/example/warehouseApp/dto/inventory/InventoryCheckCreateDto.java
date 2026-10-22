package com.example.warehouseApp.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckCreateDto {

    @NotNull(message = "Нужно выбрать склад")
    private Long warehouseId;
}