package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.movement.MovementDtoIssue;
import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.model.MovementType;

import java.time.LocalDate;

public class IssueMapper {

    public static Movement mapToEntity(MovementDtoIssue movementDtoIssue) {
        Movement movement = new Movement(MovementType.ISSUE, movementDtoIssue.getQuantity(), movementDtoIssue.getPrice(),
                movementDtoIssue.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoIssue mapToDto(Movement movement) {
        return new MovementDtoIssue(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getWarehouseFrom().getId(), movement.getCounterparty().getId(), movement.getQuantity(),
                movement.getPrice(), movement.getNumber(), movement.getDate(), movement.getNotes());
    }
}
