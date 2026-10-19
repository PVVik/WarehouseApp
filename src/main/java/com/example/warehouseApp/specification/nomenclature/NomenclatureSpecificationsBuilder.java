package com.example.warehouseApp.specification.nomenclature;

import com.example.warehouseApp.model.Nomenclature;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class NomenclatureSpecificationsBuilder extends BaseSpecificationsBuilder<Nomenclature> {

    @Override
    protected Specification<Nomenclature> createSpec(SpecSearchCriteria criterion) {
        return new NomenclatureSpecification(criterion);
    }
}
