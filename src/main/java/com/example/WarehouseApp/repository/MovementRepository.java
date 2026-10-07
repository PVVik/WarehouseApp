package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {
}
