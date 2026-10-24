package com.example.warehouseApp.util;

import com.example.warehouseApp.dto.reports.DeficitReportDto;
import com.example.warehouseApp.dto.reports.MovementReportProjection;
import com.example.warehouseApp.dto.reports.StockBalanceReportDto;
import com.example.warehouseApp.dto.reports.TurnoverReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class CsvGenerator {

    private static final String BOM = "\uFEFF";

    public byte[] generateStockBalanceCsv(List<StockBalanceReportDto> data) {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);
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
        sb.append(BOM);
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

    public byte[] generateTurnoverReportCsv(List<TurnoverReportDto> data) {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);

        sb.append("Группа ABC;Номенклатура;Расход за период (шт);Ср. мес. расход (шт);Сумма расхода (руб)\n");

        if (data != null) {
            for (TurnoverReportDto row : data) {
                sb.append(escape(row.getGroup()))
                        .append(";")
                        .append(escape(row.getNomenclatureName()))
                        .append(";")
                        .append(formatNumber(row.getTotalQuantity()))
                        .append(";")
                        .append(formatNumber(row.getAvgMonthlyQuantity()))
                        .append(";")
                        .append(formatCurrency(row.getTotalAmount()))
                        .append("\n");
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateDeficitCsv(List<DeficitReportDto> data) {
        StringBuilder sb = new StringBuilder();
        // BOM (UTF-8)
        sb.append(BOM);

        // Заголовок
        sb.append("Статус;Номенклатура;Остаток;Мин. норма;Не хватает\n");

        for (DeficitReportDto item : data) {
            String status = switch (item.getLevel()) {
                case CRITICAL -> "КРИТИЧЕСКИЙ";
                case WARNING -> "ВНИМАНИЕ";
                default -> "";
            };

            sb.append(escape(status))
                    .append(';')
                    .append(escape(item.getNomenclatureName()))
                    .append(';')
                    .append(formatNumber(item.getCurrentStock()))
                    .append(';')
                    .append(formatNumber(item.getMinThreshold()))
                    .append(';')
                    .append(formatNumber(item.getDeficitAmount()))
                    .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyDeficitCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM); // BOM
        sb.append("Статус;Номенклатура;Остаток;Мин. норма;Не хватает\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyTurnoverCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);
        sb.append("Группа ABC;Номенклатура;Расход за период (шт);Ср. мес. расход (шт);Сумма расхода (руб)\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyStockBalanceCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);
        sb.append("Номенклатура;Партия;Склад;Количество;Ед.изм.\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateEmptyMovementCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);
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

    private String formatNumber(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
        String raw = rounded.toPlainString();

        return raw;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
        String raw = rounded.toPlainString();

        if (raw.contains(".")) {
            return raw.replace('.', ',');
        }
        return raw;
    }

}