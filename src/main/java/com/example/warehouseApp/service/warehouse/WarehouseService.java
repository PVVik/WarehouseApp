package com.example.warehouseApp.service.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.service.BaseService;

public interface WarehouseService extends BaseService<WarehouseDto> {

    WarehouseDto getByName(String name);
}
