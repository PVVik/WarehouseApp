package com.example.warehouseApp.repository;

import com.example.warehouseApp.dto.reports.DeficitProjection;
import com.example.warehouseApp.dto.reports.NonLiquidProjection;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.model.StockBalance;
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

    @Query("SELECT new com.example.warehouseApp.dto.reports.StockBalanceReportDto(" +
            "sb.nomenclature.sku, " +
            "sb.nomenclature.name, " +
            "COALESCE(b.batchNumber, '-'), " +
            "sb.warehouse.name, " +
            "sb.storageLocation.code, " +
            "sb.quantity, " +
            "sb.nomenclature.unitOfMeasure) " +
            "FROM StockBalance sb " +
            "LEFT JOIN sb.batch b " +
            "WHERE sb.warehouse.id = :warehouseId " +
            "  AND sb.quantity > 0")
    List<StockBalanceReportDto> findReportByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
                SELECT
                  n.name AS nomenclatureName,
                  COALESCE(s.quantity, 0) AS currentStock,
                  n.minStock AS minThreshold
                FROM Nomenclature n
                LEFT JOIN StockBalance s ON s.nomenclature = n
                  AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
                WHERE (:warehouseId IS NULL OR s.id IS NOT NULL)
                  AND (
                    (s.quantity IS NULL OR s.quantity = 0)
                    OR
                    (n.minStock IS NOT NULL AND n.minStock > 0 AND COALESCE(s.quantity, 0) < n.minStock)
                  )
                ORDER BY n.name
            """)
    List<DeficitProjection> findDeficitsByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
                SELECT NEW com.example.warehouseApp.dto.reports.NonLiquidProjection(
                    n.id,
                    n.name,
                    n.sku,
                    w.name,
                    b.batchNumber,
                    sb.quantity,
                    n.minStock,
                    MAX(m.date)
                )
                FROM StockBalance sb
                JOIN sb.nomenclature n
                JOIN sb.warehouse w
                LEFT JOIN sb.batch b
                LEFT JOIN Movement m ON m.nomenclature = n
                                     AND (m.batch = b OR b IS NULL)
                                     AND (m.warehouseFrom = w OR m.warehouseTo = w)
                WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
                  AND sb.quantity > 0
                GROUP BY n.id, n.name, n.sku, w.name, b.batchNumber, sb.quantity, n.minStock
                ORDER BY MAX(m.date) ASC
            """)
    List<NonLiquidProjection> findNonLiquidReport(Long warehouseId);


}
