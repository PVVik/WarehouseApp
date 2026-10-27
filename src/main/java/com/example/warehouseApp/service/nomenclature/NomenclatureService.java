package com.example.warehouseApp.service.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.service.BaseService;

public interface NomenclatureService extends BaseService<NomenclatureDto> {

    NomenclatureDto getBySku(String sku);
}
