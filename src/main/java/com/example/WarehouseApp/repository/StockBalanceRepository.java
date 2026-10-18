package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    Optional<StockBalance> findByNomenclatureIdAndWarehouseIdAndStorageLocationId(Long nomenclatureId, Long warehouseId,
                                                                                  Long storageLocationId);

    Optional<StockBalance> findByNomenclatureIdAndBatchIdAndWarehouseIdAndStorageLocationId(Long nomenclatureId,
                                                                                            Long batchId,
                                                                                            Long warehouseId,
                                                                                            Long storageLocationId);

    @Query("SELECT sb FROM StockBalance sb " +
            "WHERE sb.warehouse.id = :warehouseId AND sb.quantity > 0")
    List<StockBalance> findPositiveBalancesByWarehouse(@Param("warehouseId") Long warehouseId);

}
