package com.example.WarehouseApp.specification.counterparty;

import com.example.WarehouseApp.model.Counterparty;
import com.example.WarehouseApp.specification.container.SpecSearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
public class CounterpartySpecification implements Specification<Counterparty> {

    private SpecSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Counterparty> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        String key = criteria.getKey();
        Object value = criteria.getValue();

        if (root.get(key).getJavaType() == String.class) {
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.<String>get(key)), "%" + value.toString().toLowerCase() + "%");
        } else if (root.get(key).getJavaType() == Boolean.class) {
            Boolean boolValue = Boolean.parseBoolean(value.toString());
            return criteriaBuilder.equal(root.get(key), boolValue);
        }
        return criteriaBuilder.equal(root.get(key), value);
    }
}
