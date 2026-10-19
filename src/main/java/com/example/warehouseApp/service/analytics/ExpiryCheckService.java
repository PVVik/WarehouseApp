package com.example.warehouseApp.service.analytics;

import com.example.warehouseApp.model.BatchStatus;
import com.example.warehouseApp.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpiryCheckService {

    private final BatchRepository batchRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkExpiringBatches() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);

        var expiring = batchRepository.findByExpiryDateBetween(today, threshold);
        expiring.forEach(b -> b.setStatus(BatchStatus.EXPIRING_SOON));

        batchRepository.saveAll(expiring);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkExpiredBatches() {
        LocalDate today = LocalDate.now();

        var expired = batchRepository.findByExpiryDateBefore(today);
        expired.forEach(b -> b.setStatus(BatchStatus.EXPIRED));

        batchRepository.saveAll(expired);
    }

    @Transactional(readOnly = true)
    public long countExpiringSoon() {
        LocalDate today = LocalDate.now();
        return batchRepository.countByExpiryDateBetween(today, today.plusDays(30));
    }
}
