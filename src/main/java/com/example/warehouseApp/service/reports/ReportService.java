package com.example.warehouseApp.service.reports;

import com.example.warehouseApp.dto.reports.MovementReportProjection;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final StockBalanceRepository stockBalanceRepository;
    private final MovementRepository movementRepository;

    @Transactional(readOnly = true)
    public List<StockBalanceReportDto> getStockBalanceReport(Long warehouseId) {
        log.info("Формируем отчёт остатков для склада id={}", warehouseId);
        return stockBalanceRepository.findReportByWarehouse(warehouseId);
    }

    @Transactional(readOnly = true)
    public List<MovementReportProjection> getMovementReport(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return movementRepository.findReportByWarehouseAndPeriod(warehouseId, startDate, endDate);
    }
}