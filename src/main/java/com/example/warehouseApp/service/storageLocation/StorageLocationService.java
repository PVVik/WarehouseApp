package com.example.warehouseApp.service.storageLocation;

import com.example.warehouseApp.dto.LocationWithQuantity;
import com.example.warehouseApp.dto.StorageLocationDto;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface StorageLocationService extends BaseService<StorageLocationDto> {

    BulkCreateResult createBulk(Long warehouseId, String zone, String rack, int count);

    List<LocationWithQuantity> getLocationsWithStock(Long nomenclatureId, Long warehouseId);
}
