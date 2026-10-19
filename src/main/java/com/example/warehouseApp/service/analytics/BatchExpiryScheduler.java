package com.example.warehouseApp.service.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchExpiryScheduler {

    private final ExpiryCheckService expiryCheckService;

    @Scheduled(cron = "0 0 8 * * *")
    public void dailyExpiryCheck() {
        expiryCheckService.checkExpiringBatches();
        expiryCheckService.checkExpiredBatches();
    }

}