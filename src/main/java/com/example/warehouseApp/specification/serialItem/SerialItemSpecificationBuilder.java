package com.example.warehouseApp.specification.serialItem;

import com.example.warehouseApp.model.SerialItem;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class SerialItemSpecificationBuilder extends BaseSpecificationsBuilder<SerialItem> {

    @Override
    protected Specification<SerialItem> createSpec(SpecSearchCriteria criterion) {
        return new SerialItemSpecification(criterion);
    }
}
