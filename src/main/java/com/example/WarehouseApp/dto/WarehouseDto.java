package com.example.WarehouseApp.dto;

import com.example.WarehouseApp.model.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDto {

    private Long id;

    @NotBlank(message = "Название склада обязательно")
    private String name;

    @NotBlank(message = "Адрес обязателен")
    private String address;

    @NotNull(message = "Тип склада обязателен")
    private WarehouseType type;

    private Boolean active;

}
