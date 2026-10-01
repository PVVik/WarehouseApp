package com.example.WarehouseApp.specification.warehouse;

import com.example.WarehouseApp.model.Warehouse;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class WarehouseSpecificationBuilder extends BaseSpecificationsBuilder<Warehouse> {
    @Override
    protected Specification<Warehouse> createSpec(SpecSearchCriteria criterion) {
        return new WarehouseSpecification(criterion);
    }
}
