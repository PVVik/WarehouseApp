package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.movement.MovementResponseDto;
import com.example.warehouseApp.model.Movement;

public class MovementMapper {

    public static MovementResponseDto mapToDto(Movement movement) {
        var dto = new MovementResponseDto(movement.getDate(), movement.getNumber(), movement.getMovementType().getLabel(),
                movement.getNomenclature().getName(), movement.getQuantity());

        if (movement.getBatch() != null) {
            dto.setBatchNumber(movement.getBatch().getBatchNumber());
        }

        return dto;
    }
}
