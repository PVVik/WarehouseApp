package com.example.warehouseApp.service.reports;

import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final StockBalanceRepository stockBalanceRepository;

    @Transactional(readOnly = true)
    public List<StockBalanceReportDto> getStockBalanceReport(Long warehouseId) {
        log.info("Формируем отчёт остатков для склада id={}", warehouseId);
        return stockBalanceRepository.findReportByWarehouse(warehouseId);
    }
}