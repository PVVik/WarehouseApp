package com.example.warehouseApp.service.serialItem;

import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface SerialItemService extends BaseService<SerialItemDto> {

    List<SerialItemDto> findByNomenclatureAndWarehouseWithStock(Long nomenclatureId, Long warehouseId,
                                                                ItemStatus status);
}
