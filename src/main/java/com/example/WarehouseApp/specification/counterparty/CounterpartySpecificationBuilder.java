package com.example.WarehouseApp.specification.counterparty;

import com.example.WarehouseApp.model.Counterparty;
import com.example.WarehouseApp.specification.BaseSpecificationsBuilder;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class CounterpartySpecificationBuilder extends BaseSpecificationsBuilder<Counterparty> {

    @Override
    protected Specification<Counterparty> createSpec(SpecSearchCriteria criterion) {
        return new CounterpartySpecification(criterion);
    }
}
