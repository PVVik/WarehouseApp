package com.example.warehouseApp.specification.storageLocation;

import com.example.warehouseApp.model.StorageLocation;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class StorageLocationSpecificationBuilder extends BaseSpecificationsBuilder<StorageLocation> {

    @Override
    protected Specification<StorageLocation> createSpec(SpecSearchCriteria criterion) {
        return new StorageLocationSpecification(criterion);
    }
}
