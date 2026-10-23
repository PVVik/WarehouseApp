package com.example.warehouseApp.util;

import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class CsvGenerator {

    public byte[] generateStockBalanceCsv(List<StockBalanceReportDto> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Артикул;Наименование;Партия;Склад;Место;Количество;Ед.изм.\n");

        for (StockBalanceReportDto dto : data) {
            sb.append(escape(dto.getSku())).append(";")
                    .append(escape(dto.getName())).append(";")
                    .append(escape(dto.getBatchNumber())).append(";")
                    .append(escape(dto.getWarehouseName())).append(";")
                    .append(escape(dto.getStorageLocationCode())).append(";")
                    .append(dto.getQuantity().toPlainString()).append(";")
                    .append(escape(String.valueOf(dto.getUnitOfMeasure())))
                    .append("\n");
        }

        log.debug("Сгенерирован CSV, размер: {} байт", sb.length());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}