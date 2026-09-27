package com.example.WarehouseApp.service.nomenclature;

import com.example.WarehouseApp.dto.NomenclatureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NomenclatureService {

    NomenclatureDto addNomenclature(NomenclatureDto nomenclatureDto);

    NomenclatureDto updateNomenclature(NomenclatureDto nomenclatureDto);

    Page<NomenclatureDto> getAll(Pageable pageable, String search);

    NomenclatureDto getById(long id);

    NomenclatureDto getBySku(String sku);

}
