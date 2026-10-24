package com.example.warehouseApp.controller.report;

import com.example.warehouseApp.dto.reports.MovementReportProjection;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.dto.reports.TurnoverProjection;
import com.example.warehouseApp.dto.reports.TurnoverReportDto;
import com.example.warehouseApp.service.reports.ReportService;
import com.example.warehouseApp.util.CsvGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final CsvGenerator csvGenerator;

    @GetMapping("/stock-balance")
    public List<StockBalanceReportDto> getStockJson(@RequestParam Long warehouseId) {
        if (warehouseId != null && warehouseId <= 0) {
            throw new IllegalArgumentException("warehouseId должен быть положительным числом");
        }

        return reportService.getStockBalanceReport(warehouseId);
    }

    @GetMapping(value = "/stock-balance/csv", produces = "text/csv")
    public ResponseEntity<byte[]> stockBalanceCsv(@RequestParam Long warehouseId) {
        if (warehouseId == null || warehouseId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<StockBalanceReportDto> data = reportService.getStockBalanceReport(warehouseId);

        if (data == null || data.isEmpty()) {
            byte[] emptyCsv = csvGenerator.generateEmptyStockBalanceCsv();
            HttpHeaders headers = createHeaders("stock_balance_warehouse_" + warehouseId + ".csv");
            return ResponseEntity.ok().headers(headers).body(emptyCsv);
        }

        byte[] csvBytes = csvGenerator.generateStockBalanceCsv(data);

        HttpHeaders headers = createHeaders("stock_balance_warehouse_" + warehouseId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping("/movement")
    public List<MovementReportProjection> getMovementJson(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return reportService.getMovementReport(warehouseId, startDate, endDate);
    }

    @GetMapping(value = "/movement/csv", produces = "text/csv")
    public ResponseEntity<byte[]> getMovementCsv(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        if (warehouseId != null && warehouseId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<MovementReportProjection> data = reportService.getMovementReport(warehouseId, startDate, endDate);

        String filename = "movement_report_"
                + (warehouseId != null ? "warehouse_" + warehouseId : "all_warehouses")
                + "_" + startDate + "_to_" + endDate + ".csv";

        HttpHeaders headers = createHeaders(filename);
        byte[] csvBytes;
        if (data == null || data.isEmpty()) {
            csvBytes = csvGenerator.generateEmptyMovementCsv();
        } else {
            csvBytes = csvGenerator.generateMovementReportCsv(data);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    @GetMapping("/turnover")
    public List<TurnoverReportDto> getTurnoverJson(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Параметры startDate и endDate обязательны для API-запроса");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate не может быть позже endDate");
        }

        if (warehouseId != null && warehouseId <= 0) {
            throw new IllegalArgumentException("warehouseId должен быть положительным числом или отсутствовать");
        }

        return reportService.getTurnoverReportWithAbc(warehouseId, startDate, endDate);
    }

    @GetMapping(value = "/turnover/csv", produces = "text/csv")
    public ResponseEntity<byte[]> getTurnoverCsv(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Некорректные даты".getBytes(StandardCharsets.UTF_8));
        }

        if (warehouseId != null && warehouseId <= 0) {
            return ResponseEntity.badRequest().body("Некорректный warehouseId".getBytes(StandardCharsets.UTF_8));
        }

        List<TurnoverReportDto> data = reportService.getTurnoverReportWithAbc(warehouseId, startDate, endDate);

        String filename = "turnover_report_"
                + (warehouseId != null ? "warehouse_" + warehouseId : "all_warehouses")
                + "_" + startDate + "_to_" + endDate + ".csv";

        HttpHeaders headers = createHeaders(filename);
        byte[] csvBytes;

        if (data == null || data.isEmpty()) {
            csvBytes = csvGenerator.generateEmptyTurnoverCsv();
        } else {
            csvBytes = csvGenerator.generateTurnoverReportCsv(data);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    private HttpHeaders createHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        return headers;
    }
}