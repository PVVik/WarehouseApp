package com.example.WarehouseApp.specification.nomenclature;

import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class NomenclatureSpecificationsBuilder extends BaseSpecificationsBuilder<Nomenclature> {

    @Override
    protected Specification<Nomenclature> createSpec(SpecSearchCriteria criterion) {
        return new NomenclatureSpecification(criterion);
    }
}
