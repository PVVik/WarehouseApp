package com.example.WarehouseApp.service.batch;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.dto.BatchWithStockDto;
import com.example.WarehouseApp.service.BaseService;

import java.util.List;

public interface BatchService extends BaseService<BatchDto> {

    List<BatchWithStockDto> getBatchesWithStockOnWarehouse(Long nomenclatureId, Long warehouseId);
}
