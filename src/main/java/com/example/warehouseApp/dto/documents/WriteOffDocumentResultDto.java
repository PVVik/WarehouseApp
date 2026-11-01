package com.example.warehouseApp.dto.documents;

import com.example.warehouseApp.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class WriteOffDocumentResultDto {

    private Long id;
    private DocumentStatus status;
    private String reason;
    private LocalDate updatedAt;
    private Long completedBy;
    private List<WriteOffLineBreakdownDto> breakdown;
}
