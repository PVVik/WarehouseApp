package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.DocumentSequence;
import com.example.WarehouseApp.model.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    Optional<DocumentSequence> findByMovementTypeAndYear(MovementType movementType, int year);
}
