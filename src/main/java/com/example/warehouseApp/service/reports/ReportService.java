package com.example.warehouseApp.service.reports;

import com.example.warehouseApp.dto.reports.*;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        log.info("Total amount: {}", totalAmount);
        BigDecimal cumulative = BigDecimal.ZERO;
        for (TurnoverReportDto dto : list) {
            cumulative = cumulative.add(dto.getTotalAmount());
            BigDecimal share = totalAmount.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : dto.getTotalAmount().divide(totalAmount, 4, RoundingMode.HALF_UP);
            BigDecimal cumShare = totalAmount.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : cumulative.divide(totalAmount, 4, RoundingMode.HALF_UP);

            log.info("Item: {}, amount: {}, share: {}, cumShare: {}",
                    dto.getNomenclatureName(), dto.getTotalAmount(), share, cumShare);
        }
        assignAbcGroups(list, totalAmount);

        return list;
    }

    public List<DeficitReportDto> getDeficitReport(Long warehouseId) {
        if (warehouseId == null) {
            return List.of();
        }
        List<DeficitProjection> projections = stockBalanceRepository.findDeficitsByWarehouse(warehouseId);

        return projections.stream()
                .map(this::mapToDtoWithLogic)
                .sorted((a, b) -> {
                    if (a.getLevel() == b.getLevel()) return 0;
                    if (a.getLevel() == DeficitReportDto.DeficitLevel.CRITICAL) return -1;
                    return 1;
                })
                .toList();
    }

    private DeficitReportDto mapToDtoWithLogic(DeficitProjection p) {
        DeficitReportDto dto = new DeficitReportDto();
        dto.setNomenclatureName(p.getNomenclatureName());

        BigDecimal stock = (p.getCurrentStock() != null) ? p.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal norm = (p.getMinThreshold() != null && p.getMinThreshold().compareTo(BigDecimal.ZERO) > 0)
                ? p.getMinThreshold()
                : BigDecimal.ZERO;

        dto.setCurrentStock(stock);
        dto.setMinThreshold(norm);

        if (stock.compareTo(BigDecimal.ZERO) == 0) {
            dto.setLevel(DeficitReportDto.DeficitLevel.CRITICAL);
            dto.setDeficitAmount(norm);
            return dto;
        }

        if (norm.compareTo(BigDecimal.ZERO) > 0 && stock.compareTo(norm) < 0) {
            dto.setLevel(DeficitReportDto.DeficitLevel.WARNING);
            dto.setDeficitAmount(norm.subtract(stock));
            return dto;
        }

        dto.setLevel(null);
        dto.setDeficitAmount(BigDecimal.ZERO);
        return dto;
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
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            dtos.forEach(dto -> dto.setGroup("C"));
            return;
        }

        BigDecimal cumulativeShare = BigDecimal.ZERO;
        BigDecimal thresholdA = new BigDecimal("0.80");
        BigDecimal thresholdB = new BigDecimal("0.95");

        for (TurnoverReportDto dto : dtos) {
            BigDecimal share = dto.getTotalAmount().divide(totalAmount, 4, RoundingMode.HALF_UP);

            cumulativeShare = cumulativeShare.add(share);

            if (cumulativeShare.compareTo(thresholdA) <= 0) {
                dto.setGroup("A");
            } else if (cumulativeShare.compareTo(thresholdB) <= 0) {
                dto.setGroup("B");
            } else {
                dto.setGroup("C");
            }
        }
    }
}