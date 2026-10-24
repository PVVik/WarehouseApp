package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.InventoryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLineRepository extends JpaRepository<InventoryLine, Long> {

    List<InventoryLine> findAllByCheckId(long checkId);
}
