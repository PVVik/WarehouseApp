package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.movement.MovementDtoTransfer;
import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.model.MovementType;

import java.time.LocalDate;

public class TransferMapper {

    public static Movement mapToEntity(MovementDtoTransfer movementDtoTransfer) {
        var movement = new Movement(MovementType.TRANSFER, movementDtoTransfer.getQuantity(),
                movementDtoTransfer.getUserId(), movementDtoTransfer.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoTransfer mapToDto(Movement movement) {
        return new MovementDtoTransfer(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getWarehouseFrom().getId(), movement.getWarehouseTo().getId(), movement.getQuantity(),
                movement.getNumber(), movement.getDate(), movement.getUserId(), movement.getNotes());
    }
}
