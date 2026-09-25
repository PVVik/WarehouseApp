package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
