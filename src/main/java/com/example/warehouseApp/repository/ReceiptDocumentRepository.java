package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.InventoryDiscrepancyAct;
import com.example.warehouseApp.model.ReceiptDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptDocumentRepository extends JpaRepository<ReceiptDocument, Long> {

    Optional<ReceiptDocument> findByAct(InventoryDiscrepancyAct act);

}
