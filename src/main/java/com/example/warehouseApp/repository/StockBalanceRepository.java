package com.example.warehouseApp.repository;

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
}
