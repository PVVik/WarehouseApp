package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.model.MovementType;

import java.time.LocalDate;

public class IssueMapper {

    public static Movement mapToEntity(MovementDtoIssue movementDtoIssue) {
        Movement movement = new Movement(MovementType.ISSUE, movementDtoIssue.getQuantity(), movementDtoIssue.getPrice(),
                movementDtoIssue.getUserId(), movementDtoIssue.getNotes());
        movement.setDate(LocalDate.now());

        return movement;
    }

    public static MovementDtoIssue mapToDto(Movement movement) {
        return new MovementDtoIssue(movement.getId(), movement.getMovementType(), movement.getNomenclature().getId(),
                movement.getWarehouseFrom().getId(), movement.getCounterparty().getId(), movement.getQuantity(),
                movement.getPrice(), movement.getNumber(), movement.getDate(), movement.getUserId(), movement.getNotes());
    }
}
