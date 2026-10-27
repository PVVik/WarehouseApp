package com.example.warehouseApp.service.sync;

import com.example.warehouseApp.dto.sync.SyncOperationDto;
import com.example.warehouseApp.dto.sync.SyncRequest;
import com.example.warehouseApp.dto.sync.SyncResponse;
import com.example.warehouseApp.dto.sync.SyncResultItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final SyncOperationProcessor syncOperationProcessor;

    public SyncResponse sync(SyncRequest request) {
        List<SyncResultItem> items = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < request.getOperations().size(); i++) {
            SyncOperationDto op = request.getOperations().get(i);
            SyncResultItem result = syncOperationProcessor.processOperation(i, op);
            items.add(result);

            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        return new SyncResponse(
                request.getSiteCode(),
                request.getOperations().size(),
                successCount,
                failureCount,
                items
        );
    }
}