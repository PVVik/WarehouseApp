package com.example.WarehouseApp.service.storageLocation;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.service.BaseService;

public interface StorageLocationService<S> extends BaseService<StorageLocationDto> {

    BulkCreateResult createBulk(Long warehouseId, String zone, String rack, int count);
}
