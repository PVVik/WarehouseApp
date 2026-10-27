package com.example.warehouseApp.controller.stockBalance;

import com.example.warehouseApp.dto.StockBalanceResponseDto;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockBalancesController {

    private final StockBalanceService stockBalanceService;

    @GetMapping("/stock-balances")
    public List<StockBalanceResponseDto> getStockBalances(@RequestParam(required = false) Long warehouseId) {
        return stockBalanceService.getBalances(warehouseId);
    }
}
