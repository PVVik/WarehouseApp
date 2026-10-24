package com.example.warehouseApp.controller.report;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.reports.*;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.reports.ReportService;
import com.example.warehouseApp.util.CsvGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportViewController {

    private final BaseService<WarehouseDto> warehouseService;
    private final CsvGenerator csvGenerator;
    private final ReportService reportService;

    @GetMapping("/stock-form")
    public String showStockForm(Model model) {
        model.addAttribute("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
        return "report/stock-form";
    }

    @GetMapping("/download-stock")
    public ResponseEntity<byte[]> downloadStockReport(@RequestParam(required = false) Long warehouseId) {
        List<StockBalanceReportDto> data = reportService.getStockBalanceReport(warehouseId);

        if (data == null || data.isEmpty()) {
            String emptyCsv = "Артикул;Наименование;Партия;Склад;Место;Количество;Ед.изм.\n";
            return createResponseWithBom(emptyCsv.getBytes(StandardCharsets.UTF_8), "stock_balance_report.csv");
        }

        byte[] csvBytes = csvGenerator.generateStockBalanceCsv(data);
        return createResponseWithBom(csvBytes, "stock_balance_report.csv");
    }

    @GetMapping("/stock-view")
    public String showStockView(@RequestParam(required = false) Long warehouseId, Model model) {
        model.addAttribute("warehouses", warehouseService.getAll(Pageable.unpaged(), null));

        List<StockBalanceReportDto> data = reportService.getStockBalanceReport(warehouseId);
        model.addAttribute("reportRows", data);

        model.addAttribute("selectedWarehouseId", warehouseId);

        return "report/stock-view";
    }

    @GetMapping("/movement-form")
    public String showMovementForm(Model model) {
        model.addAttribute("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
        return "report/movement-form";
    }

    @GetMapping("/download-movement")
    public ResponseEntity<byte[]> downloadMovementReport(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<MovementReportProjection> data = reportService.getMovementReport(warehouseId, startDate, endDate);

        if (data == null || data.isEmpty()) {
            String emptyCsv = "Дата;Тип документа;Номер документа;Номенклатура;Количество\n";
            return createResponseWithBom(emptyCsv.getBytes(StandardCharsets.UTF_8), "movement_report.csv");
        }

        byte[] csvBytes = csvGenerator.generateMovementReportCsv(data);
        return createResponseWithBom(csvBytes, "movement_report.csv");
    }

    @GetMapping("/movement-view")
    public String showMovementView(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Model model) {

        Page<WarehouseDto> warehouses = warehouseService.getAll(Pageable.unpaged(), null);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("selectedWarehouseId", warehouseId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        String selectedWarehouseName = "Все склады";
        if (warehouseId != null) {
            selectedWarehouseName = warehouses.stream()
                    .filter(w -> w.getId().equals(warehouseId))
                    .map(WarehouseDto::getName)
                    .findFirst()
                    .orElse("Склад не найден");
        }
        model.addAttribute("selectedWarehouseName", selectedWarehouseName);

        List<MovementReportProjection> data = reportService.getMovementReport(warehouseId, startDate, endDate);
        model.addAttribute("reportRows", data);

        return "report/movement-view";
    }

    @GetMapping("/turnover")
    public String turnoverView(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            Model model) {

        if (start == null || end == null) {
            end = LocalDate.now();
            start = end.minusDays(29);
        }

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<TurnoverReportDto> rows = reportService.getTurnoverReportWithAbc(warehouseId, start, end);
        Page<WarehouseDto> warehouses = warehouseService.getAll(Pageable.unpaged(), null);

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("turnoverList", rows);
        model.addAttribute("selectedStart", start);
        model.addAttribute("selectedEnd", end);
        model.addAttribute("selectedWarehouseId", warehouseId);

        return "report/turnover-view";
    }

    @GetMapping("/deficit")
    public String deficitView(
            @RequestParam(required = false) Long warehouseId,
            Model model) {

        Page<WarehouseDto> warehouses = warehouseService.getAll(Pageable.unpaged(), null);
        model.addAttribute("warehouses", warehouses);

        if (warehouseId == null) {
            model.addAttribute("deficitList", List.of());
            model.addAttribute("selectedWarehouseId", null);
            return "report/deficit-view";
        }

        List<DeficitReportDto> rows = reportService.getDeficitReport(warehouseId);
        model.addAttribute("deficitList", rows);
        model.addAttribute("selectedWarehouseId", warehouseId);

        return "report/deficit-view";
    }


    @GetMapping("/nonliquid")
    public String nonliquidView(
            @RequestParam(required = false) Long warehouseId,
            Model model) {

        Page<WarehouseDto> warehouses = warehouseService.getAll(Pageable.unpaged(), null);
        model.addAttribute("warehouses", warehouses);

        if (warehouseId == null) {
            model.addAttribute("nonliquidList", List.of());
            model.addAttribute("selectedWarehouseId", null);
            return "report/nonliquid-view";
        }

        List<NonLiquidReportDto> rows = reportService.getNonLiquidReport(warehouseId, LocalDate.now());
        model.addAttribute("nonliquidList", rows);
        model.addAttribute("selectedWarehouseId", warehouseId);

        return "report/nonliquid-view";
    }

    private ResponseEntity<byte[]> createResponseWithBom(byte[] content, String filename) {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] fullContent = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, fullContent, 0, bom.length);
        System.arraycopy(content, 0, fullContent, bom.length, content.length);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return new ResponseEntity<>(fullContent, headers, HttpStatus.OK);
    }
}

