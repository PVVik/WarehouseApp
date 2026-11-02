package com.example.warehouseApp.service.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.nomenclature.NomenclatureCsvRow;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface NomenclatureService extends BaseService<NomenclatureDto> {

    NomenclatureDto getBySku(String sku);

    ImportResult importAll(List<NomenclatureCsvRow> rows);

}
