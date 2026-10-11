package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    Optional<StockBalance> findByNomenclatureIdAndWarehouseId(Long nomenclatureId, Long warehouseId);
    Optional<StockBalance> findByNomenclatureIdAndBatchIdAndWarehouseId(Long nomenclatureId, Long batchId,
                                                                        Long warehouseId);
}
