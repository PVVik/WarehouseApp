package com.example.warehouseApp.dto.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultItem {

    private int index;
    private boolean success;
    private String message;
    private Long createdId;
}