package com.example.WarehouseApp.specification;

import com.example.WarehouseApp.specification.container.SearchOperation;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSpecificationsBuilder<T> {

    private final List<SpecSearchCriteria> params;

    public BaseSpecificationsBuilder() {
        params = new ArrayList<>();
    }

    public final BaseSpecificationsBuilder<T> with(String key, String operation,
                                                   Object value) {
        var op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            params.add(new SpecSearchCriteria(key, op, value));
        }
        return this;
    }

    public Specification<T> build() {
        if (params.size() == 0)
            return null;

        Specification<T> result = createSpec(params.getFirst());

        for (int i = 1; i < params.size(); i++) {
            Specification<T> nextSpec = createSpec(params.get(i));

            result = result.and(nextSpec);
        }

        return result;
    }

    protected abstract Specification<T> createSpec(SpecSearchCriteria criterion);
}
