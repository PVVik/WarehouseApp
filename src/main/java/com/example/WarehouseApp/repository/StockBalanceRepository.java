package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
}
