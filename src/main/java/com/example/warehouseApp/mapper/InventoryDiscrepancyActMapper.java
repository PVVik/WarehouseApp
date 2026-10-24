package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.inventory.DiscrepancyLineDto;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.model.DiscrepancyLine;
import com.example.warehouseApp.model.InventoryDiscrepancyAct;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryDiscrepancyActMapper {

    public static InventoryDiscrepancyActDto toDto(InventoryDiscrepancyAct act) {
        if (act == null) {
            return null;
        }
        List<DiscrepancyLine> lines = Optional.ofNullable(act.getLines())
                .orElse(Collections.emptyList());

        List<DiscrepancyLineDto> lineDtos = lines.stream()
                .filter(Objects::nonNull)
                .map(InventoryDiscrepancyActMapper::toLineDto)
                .collect(Collectors.toList());

        return new InventoryDiscrepancyActDto(act.getId(), act.getCheck().getId(),
                act.getCheck().getStatus().toString(), act.getCreatedBy(), act.getStatus(), act.getCreatedAt(),
                act.getApprovedAt(), lineDtos.size(), lineDtos);
    }

    private static DiscrepancyLineDto toLineDto(DiscrepancyLine line) {
        DiscrepancyLineDto dto = new DiscrepancyLineDto(line.getNomenclature().getId(), line.getNomenclature().getName(),
                line.getAccountedQty(), line.getActualQty(), line.getDifference(), line.getType());

        dto.setBatchNumber(line.getBatch() != null ? line.getBatch().getBatchNumber() : null);

        return dto;
    }
}