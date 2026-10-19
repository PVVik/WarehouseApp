package com.example.warehouseApp.repository;

import com.example.warehouseApp.dto.BatchWithStockDto;
import com.example.warehouseApp.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {


    @Query("""
            SELECT new com.example.warehouseApp.dto.BatchWithStockDto(
                sb.batch.id,
                sb.batch.batchNumber,
                SUM(sb.quantity)
            )
            FROM StockBalance sb
            WHERE sb.nomenclature.id = :nomenclatureId
              AND sb.warehouse.id = :warehouseId
              AND sb.quantity > 0
            GROUP BY sb.batch.id, sb.batch.batchNumber
            ORDER BY sb.batch.batchNumber
            """)
    List<BatchWithStockDto> findBatchesWithStockOnWarehouse(
            @Param("nomenclatureId") Long nomenclatureId,
            @Param("warehouseId") Long warehouseId
    );

    List<Batch> findByExpiryDateBetween(LocalDate from, LocalDate to);

    List<Batch> findByExpiryDateBefore(LocalDate date);

    long countByExpiryDateBetween(LocalDate from, LocalDate to);

}
