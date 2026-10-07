package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.model.Warehouse;
import org.flywaydb.core.internal.util.StringUtils;

import java.time.LocalDateTime;

public class WarehouseMapper {

    public static Warehouse mapToEntity(WarehouseDto warehouseDto) {
        var created = LocalDateTime.now();

        return new Warehouse(warehouseDto.getName(), warehouseDto.getAddress(), warehouseDto.getType(),
                warehouseDto.getActive(), created, created);
    }

    public static Warehouse mapToEntityWithId(WarehouseDto warehouseDto) {
        var created = LocalDateTime.now();

        return new Warehouse(warehouseDto.getId(), warehouseDto.getName(), warehouseDto.getAddress(), warehouseDto.getType(),
                warehouseDto.getActive(), created, created);
    }

    public static WarehouseDto mapToDto(Warehouse warehouse) {
        return new WarehouseDto(warehouse.getId(), warehouse.getName(), warehouse.getAddress(),
                warehouse.getType(), warehouse.isActive());
    }

    public static Warehouse mapToUpdateEntity(Warehouse warehouse, WarehouseDto warehouseDto) {
        if (StringUtils.hasText(warehouseDto.getName())) {
            warehouse.setName(warehouseDto.getName());
        }
        if (StringUtils.hasText(warehouseDto.getAddress())) {
            warehouse.setAddress(warehouseDto.getAddress());
        }
        if (warehouseDto.getType() != null) {
            warehouse.setType(warehouseDto.getType());
        }
        if (warehouseDto.getActive() != null) {
            warehouse.setActive(warehouseDto.getActive());
        }
        warehouse.setUpdatedAt(LocalDateTime.now());

        return warehouse;
    }

}
