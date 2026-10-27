package com.example.warehouseApp.specification.movement;

import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class MovementSpecificationBuilder extends BaseSpecificationsBuilder<Movement> {
    @Override
    protected Specification<Movement> createSpec(SpecSearchCriteria criterion) {
        return new MovementSpecification(criterion);
    }
}