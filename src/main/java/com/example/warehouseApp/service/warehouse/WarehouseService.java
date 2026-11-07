package com.example.warehouseApp.service.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.warehouse.WarehouseCsvRow;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface WarehouseService extends BaseService<WarehouseDto> {

    WarehouseDto getByName(String name);

    ImportResult importAll(List<WarehouseCsvRow> rows);
}
