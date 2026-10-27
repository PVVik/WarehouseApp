package com.example.warehouseApp.specification.movement;

import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.model.Warehouse;
import com.example.warehouseApp.specification.container.SearchOperation;
import com.example.warehouseApp.specification.container.SpecSearchCriteria;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
public class MovementSpecification implements Specification<Movement> {

    private SpecSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Movement> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        String key = criteria.getKey().toLowerCase();
        SearchOperation op = criteria.getOperation();
        Object value = criteria.getValue();

        switch (key) {
            case "type":
                MovementType mt = MovementType.valueOf(value.toString().toUpperCase());
                return cb.equal(root.get("movementType"), mt);

            case "datefrom":
                LocalDate dFrom = LocalDate.parse(value.toString());
                if (op == SearchOperation.GREATER_THAN) {
                    return cb.greaterThanOrEqualTo(root.get("date"), dFrom);
                }
                if (op == SearchOperation.EQUALITY) {
                    return cb.equal(root.get("date"), dFrom);
                }
                break;

            case "dateto":
                LocalDate dTo = LocalDate.parse(value.toString());
                if (op == SearchOperation.LESS_THAN) {
                    return cb.lessThanOrEqualTo(root.get("date"), dTo);
                }
                if (op == SearchOperation.EQUALITY) {
                    return cb.equal(root.get("date"), dTo);
                }
                break;

            case "quantity":
                BigDecimal qty = new BigDecimal(value.toString());
                if (op == SearchOperation.GREATER_THAN) {
                    return cb.greaterThan(root.get("quantity"), qty);
                } else if (op == SearchOperation.LESS_THAN) {
                    return cb.lessThan(root.get("quantity"), qty);
                } else if (op == SearchOperation.EQUALITY) {
                    return cb.equal(root.get("quantity"), qty);
                }
                break;

            case "warehouseid":
                Long wId = Long.parseLong(value.toString());
                Join<Movement, Warehouse> join = root.join("warehouseFrom", JoinType.LEFT);
                return cb.equal(join.get("id"), wId);

            default:
                return cb.equal(root.get(key), value);
        }

        return cb.conjunction();
    }
}
