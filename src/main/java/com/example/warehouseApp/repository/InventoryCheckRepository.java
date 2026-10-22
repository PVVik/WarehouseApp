package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.InventoryCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {
}
