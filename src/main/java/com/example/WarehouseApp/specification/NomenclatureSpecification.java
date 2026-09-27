package com.example.WarehouseApp.specification;

import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.specification.container.SearchOperation;
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
        SearchOperation op = criteria.getOperation();
        String key = criteria.getKey();
        Object value = criteria.getValue();

        return switch (op) {
            case CONTAINS -> builder.like(
                    builder.lower(root.<String>get(key)), "%" + value.toString().toLowerCase() + "%");
            case STARTS_WITH -> builder.like(
                    builder.lower(root.<String>get(key)), value.toString().toLowerCase() + "%");
            case ENDS_WITH -> builder.like(
                    builder.lower(root.<String>get(key)), "%" + value.toString().toLowerCase());
            case EQUALITY -> {
                if (root.get(key).getJavaType() == String.class) {
                    yield builder.like(
                            builder.lower(root.<String>get(key)), "%" + value.toString().toLowerCase() + "%");
                } else if (root.get(key).getJavaType() == Boolean.class) {
                    Boolean boolValue = Boolean.parseBoolean(value.toString());
                    yield builder.equal(root.get(key), boolValue);
                }
                yield builder.equal(root.get(key), value);
            }
            case GREATER_THAN -> builder.greaterThanOrEqualTo(builder.lower(root.<String>get(key)),
                    value.toString().toLowerCase());
            case LESS_THAN -> builder.lessThanOrEqualTo(builder.lower(root.<String>get(key)),
                    value.toString().toLowerCase());
            default -> null;
        };
    }
}
