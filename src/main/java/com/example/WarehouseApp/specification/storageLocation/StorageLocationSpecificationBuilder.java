package com.example.WarehouseApp.specification.storageLocation;

import com.example.WarehouseApp.model.StorageLocation;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class StorageLocationSpecificationBuilder extends BaseSpecificationsBuilder<StorageLocation> {

    @Override
    protected Specification<StorageLocation> createSpec(SpecSearchCriteria criterion) {
        return new StorageLocationSpecification(criterion);
    }
}
