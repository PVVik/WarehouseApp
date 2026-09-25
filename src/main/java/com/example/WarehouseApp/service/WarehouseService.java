package com.example.WarehouseApp.service;

import com.example.WarehouseApp.dto.WarehouseDto;

import java.util.List;

public interface WarehouseService {

    WarehouseDto addWarehouse(WarehouseDto warehouseDto);

    WarehouseDto updateWarehouse(WarehouseDto warehouseDto);

    WarehouseDto getWarehouseById(long id);

    List<WarehouseDto> getWarehouses();

    void deleteWarehouse(long id);
}
