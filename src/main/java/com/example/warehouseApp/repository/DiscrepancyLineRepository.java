package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.DiscrepancyLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscrepancyLineRepository extends JpaRepository<DiscrepancyLine, Long> {
}
