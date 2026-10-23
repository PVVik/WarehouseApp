package com.example.warehouseApp.controller.report;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.reports.ReportService;
import com.example.warehouseApp.util.CsvGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportViewController {

    private final BaseService<WarehouseDto> warehouseService;
    private final CsvGenerator csvGenerator;
    private final ReportService reportService;

    @GetMapping("/form")
    public String showReportForm(Model model) {
        model.addAttribute("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
        return "report/report-form";
    }

    @GetMapping("/form/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam(required = false) Long warehouseId) {
        List<StockBalanceReportDto> data = reportService.getStockBalanceReport(warehouseId);

        if (data == null || data.isEmpty()) {
            String emptyCsv = "Артикул;Наименование;Партия;Склад;Место;Количество;Ед.изм.\n";
            return createResponseWithBom(emptyCsv.getBytes(StandardCharsets.UTF_8));
        }

        byte[] csvBytes = csvGenerator.generateStockBalanceCsv(data);

        return createResponseWithBom(csvBytes);
    }

    private ResponseEntity<byte[]> createResponseWithBom(byte[] content) {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] fullContent = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, fullContent, 0, bom.length);
        System.arraycopy(content, 0, fullContent, bom.length, content.length);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stock_balance_report.csv\"");

        return new ResponseEntity<>(fullContent, headers, HttpStatus.OK);
    }
}
