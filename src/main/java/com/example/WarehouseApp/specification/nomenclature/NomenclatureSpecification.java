package com.example.WarehouseApp.specification.nomenclature;

import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
public class NomenclatureSpecification implements Specification<Nomenclature> {

    private SpecSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Nomenclature> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String key = criteria.getKey();
        Object value = criteria.getValue();

        if (root.get(key).getJavaType() == String.class) {
            return builder.like(
                    builder.lower(root.<String>get(key)), "%" + value.toString().toLowerCase() + "%");
        } else if (root.get(key).getJavaType() == Boolean.class) {
            Boolean boolValue = Boolean.parseBoolean(value.toString());
            return builder.equal(root.get(key), boolValue);
        }
        return builder.equal(root.get(key), value);

    }
}
