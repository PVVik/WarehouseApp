package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.model.Nomenclature;
import org.flywaydb.core.internal.util.StringUtils;

public class NomenclatureMapper {

    public static Nomenclature mapToEntity(NomenclatureDto nomenclatureDto) {
        Nomenclature nomenclature = new Nomenclature(nomenclatureDto.getName(), nomenclatureDto.getSku(),
                nomenclatureDto.getStuffCategory(), nomenclatureDto.getUnitOfMeasure(),
                nomenclatureDto.getInventoryType(), nomenclatureDto.getActive());
        if (nomenclatureDto.getMinStock() != null) {
            nomenclature.setMinStock(nomenclatureDto.getMinStock());
        }

        return nomenclature;
    }

    public static Nomenclature mapToEntityWithId(NomenclatureDto nomenclatureDto) {
        Nomenclature nomenclature = new Nomenclature(nomenclatureDto.getId(), nomenclatureDto.getName(), nomenclatureDto.getSku(),
                nomenclatureDto.getStuffCategory(), nomenclatureDto.getUnitOfMeasure(),
                nomenclatureDto.getInventoryType(), nomenclatureDto.getActive());
        if (nomenclatureDto.getMinStock() != null) {
            nomenclature.setMinStock(nomenclatureDto.getMinStock());
        }

        return nomenclature;
    }

    public static NomenclatureDto mapToDto(Nomenclature nomenclature) {
        NomenclatureDto dto = new NomenclatureDto(nomenclature.getId(), nomenclature.getName(), nomenclature.getSku(),
                nomenclature.getStuffCategory(), nomenclature.getUnitOfMeasure(), nomenclature.getInventoryType(),
                nomenclature.isActive(), nomenclature.getStuffCategory().getLabel(),
                nomenclature.getUnitOfMeasure().getLabel(), nomenclature.getInventoryType().getLabel());
        if (nomenclature.getMinStock() != null) {
            dto.setMinStock(nomenclature.getMinStock());
        }

        return dto;
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
        if (nomenclatureDto.getMinStock() != null) {
            nomenclature.setMinStock(nomenclatureDto.getMinStock());
        }

        return nomenclature;
    }
}
