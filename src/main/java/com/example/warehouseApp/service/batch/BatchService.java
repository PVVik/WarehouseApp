package com.example.warehouseApp.service.batch;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.BatchWithStockDto;
import com.example.warehouseApp.model.Batch;
import com.example.warehouseApp.service.BaseService;

import java.time.LocalDate;
import java.util.List;

public interface BatchService extends BaseService<BatchDto> {

    List<BatchWithStockDto> getBatchesWithStockOnWarehouse(Long nomenclatureId, Long warehouseId);

}
