package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.model.Movement;

import java.time.LocalDate;

public class ReceiptMapper {

    public static Movement mapToEntity(MovementDtoReceipt movementDtoReceipt) {
        Movement movement = new Movement(movementDtoReceipt.getMovementType(), movementDtoReceipt.getQuantity(),
                movementDtoReceipt.getPrice(), movementDtoReceipt.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoReceipt mapToDto(Movement movement) {
        return new MovementDtoReceipt(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getWarehouseTo().getId(), movement.getCounterparty().getId(), movement.getQuantity(),
                movement.getPrice(), movement.getNumber(), movement.getDate(), movement.getNotes());
    }
}
