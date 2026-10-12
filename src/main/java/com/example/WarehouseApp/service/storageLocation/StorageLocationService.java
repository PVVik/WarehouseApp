package com.example.WarehouseApp.service.storageLocation;

import com.example.WarehouseApp.dto.LocationWithQuantity;
import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.service.BaseService;

import java.util.List;

public interface StorageLocationService extends BaseService<StorageLocationDto> {

    BulkCreateResult createBulk(Long warehouseId, String zone, String rack, int count);

    List<LocationWithQuantity> getLocationsWithStock(Long nomenclatureId, Long warehouseId);
}
