package com.example.warehouseApp.service.reports;

import com.example.warehouseApp.dto.reports.MovementReportProjection;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.dto.reports.TurnoverProjection;
import com.example.warehouseApp.dto.reports.TurnoverReportDto;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final StockBalanceRepository stockBalanceRepository;
    private final MovementRepository movementRepository;
    private static final double THRESHOLD_A_PERCENT = 0.70;
    private static final double THRESHOLD_B_PERCENT = 0.90;
    private static final double DAYS_IN_MONTH = 30.0;

    @Transactional(readOnly = true)
    public List<StockBalanceReportDto> getStockBalanceReport(Long warehouseId) {
        log.info("Формируем отчёт остатков для склада id={}", warehouseId);
        return stockBalanceRepository.findReportByWarehouse(warehouseId);
    }

    @Transactional(readOnly = true)
    public List<MovementReportProjection> getMovementReport(Long warehouseId, LocalDate startDate, LocalDate endDate) {
        return movementRepository.findReportByWarehouseAndPeriod(warehouseId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<TurnoverReportDto> getTurnoverReportWithAbc(Long warehouseId, LocalDate start, LocalDate end) {
        double daysInPeriod = ChronoUnit.DAYS.between(start, end) + 1;

        List<TurnoverProjection> projectionList = movementRepository.findTurnoverDataByPeriod(
                warehouseId, start, end, daysInPeriod
        );

        if (projectionList.isEmpty()) {
            return List.of();
        }

        List<TurnoverReportDto> list = convertProjectionsToDtos(projectionList);

        BigDecimal totalAmount = calculateTotalAmount(list);
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            list.forEach(dto -> dto.setGroup("C"));
            return list;
        }

        assignAbcGroups(list, totalAmount);

        return list;
    }

    private List<TurnoverReportDto> convertProjectionsToDtos(List<TurnoverProjection> projections) {
        return projections.stream()
                .map(p -> new TurnoverReportDto(
                        p.getNomenclatureId(),
                        p.getNomenclatureName(),
                        TurnoverReportDto.zeroIfNull(p.getTotalQuantity()),
                        TurnoverReportDto.zeroIfNull(p.getAvgMonthlyQuantity()),
                        TurnoverReportDto.zeroIfNull(p.getTotalAmount()),
                        null
                ))
                .sorted(Comparator.comparing(TurnoverReportDto::getTotalAmount, Comparator.reverseOrder()))
                .toList();
    }

    private BigDecimal calculateTotalAmount(List<TurnoverReportDto> dtos) {
        return dtos.stream()
                .map(TurnoverReportDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assignAbcGroups(List<TurnoverReportDto> dtos, BigDecimal totalAmount) {
        BigDecimal thresholdA = totalAmount.multiply(BigDecimal.valueOf(THRESHOLD_A_PERCENT));
        BigDecimal thresholdB = totalAmount.multiply(BigDecimal.valueOf(THRESHOLD_B_PERCENT));

        BigDecimal cumulative = BigDecimal.ZERO;

        for (TurnoverReportDto dto : dtos) {
            cumulative = cumulative.add(dto.getTotalAmount());

            if (cumulative.compareTo(thresholdA) <= 0) {
                dto.setGroup("A");
            } else if (cumulative.compareTo(thresholdB) <= 0) {
                dto.setGroup("B");
            } else {
                dto.setGroup("C");
            }
        }
    }
}