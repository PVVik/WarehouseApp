package com.example.WarehouseApp.specification.serialItem;

import com.example.WarehouseApp.model.SerialItem;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class SerialItemSpecificationBuilder extends BaseSpecificationsBuilder<SerialItem> {

    @Override
    protected Specification<SerialItem> createSpec(SpecSearchCriteria criterion) {
        return new SerialItemSpecification(criterion);
    }
}
