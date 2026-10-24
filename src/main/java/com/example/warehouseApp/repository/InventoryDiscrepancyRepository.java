package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.InventoryDiscrepancyAct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryDiscrepancyRepository extends JpaRepository<InventoryDiscrepancyAct, Long> {
}
