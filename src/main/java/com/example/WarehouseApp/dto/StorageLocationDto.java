package com.example.WarehouseApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StorageLocationDto implements Comparable<StorageLocationDto> {

    private Long id;

    @NotNull(message = "Данные склада обязательны для заполнения")
    private Long warehouseId;

    private String warehouseName;

    @NotBlank(message = "Зона склада должна быть указана")
    private String zone;

    @NotBlank(message = "Стеллаж должен быть указан")
    private String rack;

    @NotBlank(message = "Полка должна быть указана")
    private String shelf;

    private String code;

    @NotNull(message = "Укажите, занята ли ячейка")
    private Boolean used;

    public StorageLocationDto(Long id, Long warehouseId, String zone, String rack, String shelf, String code, boolean used) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.zone = zone;
        this.rack = rack;
        this.shelf = shelf;
        this.code = code;
        this.used = used;
    }

    @Override
    public int compareTo(StorageLocationDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
