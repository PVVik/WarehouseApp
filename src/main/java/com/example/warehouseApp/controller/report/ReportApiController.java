package com.example.warehouseApp.controller.report;

import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.service.reports.ReportService;
import com.example.warehouseApp.util.CsvGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final CsvGenerator csvGenerator;

    @GetMapping(value = "/stock-balance/csv", produces = "text/csv")
    public byte[] stockBalanceCsv(@RequestParam Long warehouseId) {
        List<StockBalanceReportDto> data = reportService.getStockBalanceReport(warehouseId);

        return csvGenerator.generateStockBalanceCsv(data);
    }
}