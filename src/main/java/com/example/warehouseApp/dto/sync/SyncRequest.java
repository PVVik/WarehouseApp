package com.example.warehouseApp.dto.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {

    private String siteCode;
    private String syncToken;
    private List<SyncOperationDto> operations;
}