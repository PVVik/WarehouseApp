package com.example.warehouseApp.mapper;

import com.example.warehouseApp.dto.StockBalanceResponseDto;
import com.example.warehouseApp.model.StockBalance;

public class StockBalanceMapper {

    public static StockBalanceResponseDto mapToDto(StockBalance stockBalance) {
        var dto = new StockBalanceResponseDto(stockBalance.getNomenclature().getSku(),
                stockBalance.getNomenclature().getName(), stockBalance.getWarehouse().getName(),
                stockBalance.getQuantity(), stockBalance.getNomenclature().getUnitOfMeasure().getLabel());

        if (stockBalance.getBatch() != null) {
            dto.setBatchNumber(stockBalance.getBatch().getBatchNumber());
        }

        return dto;
    }
}
