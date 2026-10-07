package com.example.WarehouseApp.service.storageLocation;

public record BulkCreateResult(boolean success, int created, int skipped, String message) {
}
