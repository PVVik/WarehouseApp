package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.InventoryDiscrepancyAct;
import com.example.warehouseApp.model.WriteOffDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WriteOffDocumentRepository extends JpaRepository<WriteOffDocument, Long> {

    Optional<WriteOffDocument> findByAct(InventoryDiscrepancyAct act);
}
