package com.example.warehouseApp.specification.counterparty;

import com.example.warehouseApp.model.Counterparty;
import com.example.warehouseApp.specification.BaseSpecificationsBuilder;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class CounterpartySpecificationBuilder extends BaseSpecificationsBuilder<Counterparty> {

    @Override
    protected Specification<Counterparty> createSpec(SpecSearchCriteria criterion) {
        return new CounterpartySpecification(criterion);
    }
}
