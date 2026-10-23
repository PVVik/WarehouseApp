package com.example.warehouseApp.dto.inventory;

import com.example.warehouseApp.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDiscrepancyActDto {
    private Long id;
    private Long checkId;
    private String checkStatus;
    private Long createdBy;
    private DocumentStatus status;
    private LocalDate createdAt;
    private LocalDate approvedAt;
    private long totalLines;
    private List<DiscrepancyLineDto> lines;
}