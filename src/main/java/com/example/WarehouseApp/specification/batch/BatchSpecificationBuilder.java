package com.example.WarehouseApp.specification.batch;

import com.example.WarehouseApp.model.Batch;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class BatchSpecificationBuilder extends BaseSpecificationsBuilder<Batch> {
    @Override
    protected Specification<Batch> createSpec(SpecSearchCriteria criterion) {
        return new BatchSpecification(criterion);
    }
}
