package com.example.warehouseApp.service.storageLocation;

public record BulkCreateResult(boolean success, int created, int skipped, String message) {
}
