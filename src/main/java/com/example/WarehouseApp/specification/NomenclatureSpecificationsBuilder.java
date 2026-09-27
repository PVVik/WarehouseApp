package com.example.WarehouseApp.specification;

import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.specification.container.SearchOperation;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NomenclatureSpecificationsBuilder {

    private final List<SpecSearchCriteria> params;

    public NomenclatureSpecificationsBuilder() {
        params = new ArrayList<>();
    }

    public final NomenclatureSpecificationsBuilder with(String key, String operation,
                                                        Object value) {
        var op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            params.add(new SpecSearchCriteria(key, op, value));
        }
        return this;
    }

    public Specification<Nomenclature> build() {
        if (params.size() == 0)
            return null;

        Specification<Nomenclature> result = new NomenclatureSpecification(params.getFirst());

        for (int i = 1; i < params.size(); i++) {
            NomenclatureSpecification nextSpec = new NomenclatureSpecification(params.get(i));

            result = result.and(nextSpec);
        }

        return result;
    }
}
