package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.ReceiptDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptDocumentRepository extends JpaRepository<ReceiptDocument, Long> {
}
