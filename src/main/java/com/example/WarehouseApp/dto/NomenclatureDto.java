package com.example.WarehouseApp.dto;

import com.example.WarehouseApp.model.InventoryType;
import com.example.WarehouseApp.model.StuffCategory;
import com.example.WarehouseApp.model.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NomenclatureDto implements Comparable<NomenclatureDto> {

    private Long id;

    @NotBlank(message = "Название обязательно для заполнения")
    private String name;

    @NotBlank(message = "Артикул (SKU) обязателен для заполнения")
    private String sku;

    @NotNull(message = "Категория номенклатуры обязательна")
    private StuffCategory stuffCategory;

    @NotNull(message = "Единица измерения обязательна")
    private UnitOfMeasure unitOfMeasure;

    @NotNull(message = "Тип учета обязателен")
    private InventoryType inventoryType;

    private Boolean active;
    private String stuffCategoryLabel;
    private String unitOfMeasureLabel;
    private String inventoryTypeLabel;

    @Override
    public int compareTo(NomenclatureDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
