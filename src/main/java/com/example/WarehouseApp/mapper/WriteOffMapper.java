package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.movement.MovementDtoWriteOff;
import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.model.MovementType;

import java.time.LocalDate;

public class WriteOffMapper {

    public static Movement mapToEntity(MovementDtoWriteOff movementDtoWriteOff) {
        Movement movement = new Movement(MovementType.WRITE_OFF, movementDtoWriteOff.getQuantity(),
                movementDtoWriteOff.getUserId(), movementDtoWriteOff.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoWriteOff mapToDto(Movement movement) {
        return new MovementDtoWriteOff(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getQuantity(), movement.getWarehouseFrom().getId(), movement.getStorageLocation().getId(),
                movement.getNotes(), movement.getNumber(), movement.getDate(), movement.getUserId());
    }
}
