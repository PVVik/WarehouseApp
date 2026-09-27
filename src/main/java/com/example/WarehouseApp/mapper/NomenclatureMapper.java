package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.model.Nomenclature;
import org.flywaydb.core.internal.util.StringUtils;

public class NomenclatureMapper {

    public static Nomenclature mapToEntity(NomenclatureDto nomenclatureDto) {
        return new Nomenclature(nomenclatureDto.getName(), nomenclatureDto.getSku(),
                nomenclatureDto.getStuffCategory(), nomenclatureDto.getUnitOfMeasure(),
                nomenclatureDto.getInventoryType(), nomenclatureDto.getActive());
    }

    public static NomenclatureDto mapToDto(Nomenclature nomenclature) {
        return new NomenclatureDto(nomenclature.getId(), nomenclature.getName(), nomenclature.getSku(),
                nomenclature.getStuffCategory(), nomenclature.getUnitOfMeasure(), nomenclature.getInventoryType(),
                nomenclature.isActive(), nomenclature.getStuffCategory().getLabel(),
                nomenclature.getUnitOfMeasure().getFullName(), nomenclature.getInventoryType().getLabel());
    }

    public static Nomenclature mapToUpdateEntity(Nomenclature nomenclature, NomenclatureDto nomenclatureDto) {
        if (StringUtils.hasText(nomenclatureDto.getName())) {
            nomenclature.setName(nomenclatureDto.getName());
        }
        if (StringUtils.hasText(nomenclatureDto.getSku())) {
            nomenclature.setSku(nomenclatureDto.getSku());
        }
        if (nomenclatureDto.getStuffCategory() != null) {
            nomenclature.setStuffCategory(nomenclatureDto.getStuffCategory());
        }
        if (nomenclatureDto.getUnitOfMeasure() != null) {
            nomenclature.setUnitOfMeasure(nomenclatureDto.getUnitOfMeasure());
        }
        if (nomenclatureDto.getInventoryType() != null) {
            nomenclature.setInventoryType(nomenclatureDto.getInventoryType());
        }
        if (nomenclatureDto.getActive() != null) {
            nomenclature.setActive(nomenclatureDto.getActive());
        }

        return nomenclature;
    }
}
