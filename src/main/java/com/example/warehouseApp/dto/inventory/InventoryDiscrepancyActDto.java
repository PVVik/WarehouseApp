package com.example.warehouseApp.dto.inventory;

import com.example.warehouseApp.model.DiscrepancyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDiscrepancyActDto {
    private Long id;
    private Long checkId;
    private String checkStatus;
    private Long createdBy;
    private DiscrepancyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private long totalLines;
    private List<DiscrepancyLineDto> lines;
}