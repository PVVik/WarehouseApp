package com.example.warehouseApp.dto;

import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.StuffCategory;
import com.example.warehouseApp.model.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal minStock;
    private String stuffCategoryLabel;
    private String unitOfMeasureLabel;
    private String inventoryTypeLabel;

    public NomenclatureDto(Long id, String name, String sku, StuffCategory stuffCategory, UnitOfMeasure unitOfMeasure,
                           InventoryType inventoryType, Boolean active, String stuffCategoryLabel,
                           String unitOfMeasureLabel, String inventoryTypeLabel) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.stuffCategory = stuffCategory;
        this.unitOfMeasure = unitOfMeasure;
        this.inventoryType = inventoryType;
        this.active = active;
        this.stuffCategoryLabel = stuffCategoryLabel;
        this.unitOfMeasureLabel = unitOfMeasureLabel;
        this.inventoryTypeLabel = inventoryTypeLabel;
    }

    @Override
    public int compareTo(NomenclatureDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
