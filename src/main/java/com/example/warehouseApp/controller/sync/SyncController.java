package com.example.warehouseApp.controller.sync;

import com.example.warehouseApp.dto.sync.SyncRequest;
import com.example.warehouseApp.dto.sync.SyncResponse;
import com.example.warehouseApp.service.sync.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> sync(@RequestBody SyncRequest request) {
        SyncResponse response = syncService.sync(request);

        return ResponseEntity.ok(response);
    }
}
