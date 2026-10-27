package com.example.warehouseApp.dto.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {
    private String siteCode;
    private int totalCount;
    private int successCount;
    private int failureCount;
    private List<SyncResultItem> results;
}