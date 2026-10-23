package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.WriteOffDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WriteOffDocumentRepository extends JpaRepository<WriteOffDocument, Long> {
}
