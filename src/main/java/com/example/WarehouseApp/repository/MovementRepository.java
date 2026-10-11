package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.dto.LocationBalanceDto;
import com.example.WarehouseApp.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {


    @Query("""
    SELECT
        new com.example.WarehouseApp.dto.LocationBalanceDto(
            sl.id,
            COALESCE(sl.code, CONCAT(sl.zone, '-', sl.rack, '-', sl.shelf)),
            SUM(CASE WHEN m.movementType = 'RECEIPT' THEN m.quantity WHEN m.movementType = 'ISSUE' THEN -m.quantity ELSE 0 END)
        )
    FROM Movement m
    JOIN m.storageLocation sl
    WHERE m.nomenclature.id = :nomenclatureId
      AND m.warehouseTo.id = :warehouseId
    GROUP BY sl.id, sl.code, sl.zone, sl.rack, sl.shelf
    HAVING SUM(CASE WHEN m.movementType = 'RECEIPT' THEN m.quantity WHEN m.movementType = 'ISSUE' THEN -m.quantity ELSE 0 END) > 0
    ORDER BY sl.code
""")
    List<LocationBalanceDto> findLocationsWithPositiveBalance(
            @Param("nomenclatureId") Long nomenclatureId,
            @Param("warehouseId") Long warehouseId
    );
}
