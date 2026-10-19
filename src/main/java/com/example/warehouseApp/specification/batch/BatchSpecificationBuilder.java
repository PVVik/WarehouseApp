package com.example.warehouseApp.specification.batch;

import com.example.warehouseApp.model.Batch;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class BatchSpecificationBuilder extends BaseSpecificationsBuilder<Batch> {
    @Override
    protected Specification<Batch> createSpec(SpecSearchCriteria criterion) {
        return new BatchSpecification(criterion);
    }
}
