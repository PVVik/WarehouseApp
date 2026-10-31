package com.example.warehouseApp.dto.documents;

import com.example.warehouseApp.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDocumentResultDto {

    private Long id;
    private DocumentStatus status;
    private String source;
    private LocalDate updatedAt;
    private Long completedBy;
    private List<ReceiptLineBreakdownDto> breakdown;
}