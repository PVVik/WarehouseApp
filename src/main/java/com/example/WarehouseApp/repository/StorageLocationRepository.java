package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long>,
        JpaSpecificationExecutor<StorageLocation> {
}
