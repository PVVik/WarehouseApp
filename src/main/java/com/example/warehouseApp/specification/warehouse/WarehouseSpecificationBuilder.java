package com.example.warehouseApp.specification.warehouse;

import com.example.warehouseApp.model.Warehouse;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class WarehouseSpecificationBuilder extends BaseSpecificationsBuilder<Warehouse> {
    @Override
    protected Specification<Warehouse> createSpec(SpecSearchCriteria criterion) {
        return new WarehouseSpecification(criterion);
    }
}
