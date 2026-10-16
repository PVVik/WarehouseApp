package com.example.WarehouseApp.service.serialItem;

import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.service.BaseService;

import java.util.List;

public interface SerialItemService extends BaseService<SerialItemDto> {

    List<SerialItemDto> findByNomenclatureAndWarehouseWithStock(Long nomenclatureId, Long warehouseId,
                                                                ItemStatus status);
}
