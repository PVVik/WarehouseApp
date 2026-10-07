package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.model.Movement;

import java.time.LocalDate;

public class ReceiptMapper {

    public static Movement mapToEntity(MovementDtoReceipt movementDtoReceipt) {
        Movement movement = new Movement(movementDtoReceipt.getMovementType(), movementDtoReceipt.getQuantity(),
                movementDtoReceipt.getPrice(), movementDtoReceipt.getUserId(), movementDtoReceipt.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoReceipt mapToDto(Movement movement) {
        return new MovementDtoReceipt(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getWarehouseTo().getId(), movement.getCounterparty().getId(),
                movement.getQuantity(), movement.getPrice(), movement.getNumber(), movement.getDate(),
                movement.getUserId(), movement.getNotes());
    }
}
