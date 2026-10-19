package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.DocumentSequence;
import com.example.warehouseApp.model.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    Optional<DocumentSequence> findByMovementTypeAndYear(MovementType movementType, int year);
}
