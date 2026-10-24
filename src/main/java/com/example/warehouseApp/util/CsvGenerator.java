package com.example.warehouseApp.util;

import com.example.warehouseApp.dto.reports.MovementReportProjection;
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
        sb.append("\uFEFF");
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

    public byte[] generateMovementReportCsv(List<MovementReportProjection> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("Дата;Тип документа;Номер документа;Номенклатура;Количество\n");

        for (MovementReportProjection p : data) {
            String typeLabel = (p.getDocumentType() != null) ? p.getDocumentType().getLabel() : "";

            sb.append(escape(p.getDate() != null ? p.getDate().toString() : ""))
                    .append(";")
                    .append(escape(typeLabel))
                    .append(";")
                    .append(escape(p.getDocumentNumber()))
                    .append(";")
                    .append(escape(p.getNomenclatureName()))
                    .append(";")
                    .append(p.getQty() != null ? p.getQty().toPlainString() : "0")
                    .append("\n");
        }

        log.debug("Сгенерирован отчёт по движениям, размер: {} байт", sb.length());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyStockBalanceCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("Номенклатура;Партия;Склад;Количество;Ед.изм.\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyMovementCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("Дата;Тип документа;Номер документа;Номенклатура;Партия;Склад;Количество;Ед.изм.\n");
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