package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.dto.LocationWithQuantity;
import com.example.WarehouseApp.model.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long>,
        JpaSpecificationExecutor<StorageLocation> {

    @Query("SELECT new com.example.WarehouseApp.dto.LocationWithQuantity(" +
            "sl.id, " +
            "COALESCE(sl.code, CONCAT(sl.zone, '-', sl.rack, '-', sl.shelf)), " +
            "sb.quantity" +
            ") " +
            "FROM StorageLocation sl " +
            "JOIN StockBalance sb ON sl.id = sb.storageLocation.id " + // <-- вот тут исправлено
            "WHERE sb.nomenclature.id = :nomenclatureId " +           // <-- тоже лучше через связь
            "AND sb.warehouse.id = :warehouseId " +                    // <-- и тут
            "AND sb.quantity > 0 " +
            "ORDER BY sb.quantity DESC")
    List<LocationWithQuantity> findLocationsWithQuantity(
            @Param("nomenclatureId") Long nomenclatureId,
            @Param("warehouseId") Long warehouseId
    );
}
