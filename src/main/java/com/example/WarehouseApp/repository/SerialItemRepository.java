package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.SerialItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SerialItemRepository extends JpaRepository<SerialItem, Long>, JpaSpecificationExecutor<SerialItem> {
}
