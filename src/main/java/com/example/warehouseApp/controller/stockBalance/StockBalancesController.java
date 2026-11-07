package com.example.warehouseApp.controller.stockBalance;

import com.example.warehouseApp.dto.StockBalanceResponseDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.stockBalance.StockBalanceCsvParser;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-balances")
@RequiredArgsConstructor
public class StockBalancesController {

    private final StockBalanceService stockBalanceService;
    private final StockBalanceCsvParser stockBalanceCsvParser;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public List<StockBalanceResponseDto> getStockBalances(@RequestParam(required = false) Long warehouseId) {
        return stockBalanceService.getBalances(warehouseId);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран или пуст"));
        }

        StockBalanceCsvParser.ParseResult parsed;
        try {
            parsed = stockBalanceCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        ImportResult result = stockBalanceService.importAll(parsed.rows());

        var body = new LinkedHashMap<String, Object>();
        body.put("created", result.created());
        body.put("updated", result.updated());
        body.put("imported", result.created() + result.updated());
        if (parsed.hasErrors()) {
            body.put("skipped", parsed.errors().size());
            body.put("errors", parsed.errors());
        }
        return ResponseEntity.ok(body);
    }
}
