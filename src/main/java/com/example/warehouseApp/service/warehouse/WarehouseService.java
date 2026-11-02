package com.example.warehouseApp.service.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.parser.WarehouseCsvRow;
import com.example.warehouseApp.parser.WarehouseImportResult;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface WarehouseService extends BaseService<WarehouseDto> {

    WarehouseDto getByName(String name);

    WarehouseImportResult importAll(List<WarehouseCsvRow> rows);
}
