package com.example.warehouseApp.parser.nomenclature;

import com.example.warehouseApp.mapper.LabeledEnums;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.StuffCategory;
import com.example.warehouseApp.model.UnitOfMeasure;
import com.example.warehouseApp.parser.CsvFiles;
import com.example.warehouseApp.parser.CsvHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class NomenclatureCsvParser {

    private static final String SEPARATOR = ";";

    public record ParseResult(List<NomenclatureCsvRow> rows, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public ParseResult parse(MultipartFile file) throws IOException {
        List<String> lines = CsvFiles.readLines(file);
        List<String> errors = new ArrayList<>();

        if (lines.isEmpty()) {
            errors.add("Файл пуст");
            return new ParseResult(List.of(), errors);
        }

        Map<String, Integer> header = CsvHeaders.parse(lines.get(0));
        Integer nameIdx = CsvHeaders.find(header, "название", "наименование", "name");
        Integer skuIdx = CsvHeaders.find(header, "артикул", "sku");
        Integer categoryIdx = CsvHeaders.find(header, "категория", "category");
        Integer unitIdx = CsvHeaders.find(header, "ед.измерения", "ед. измерения", "единица измерения", "unit");
        Integer typeIdx = CsvHeaders.find(header, "тип", "type");
        Integer minStockIdx = CsvHeaders.find(header, "мин.количество", "мин. количество",
                "мин количество", "мин.остаток", "minstock");

        if (nameIdx == null || skuIdx == null || categoryIdx == null || unitIdx == null || typeIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: название;артикул;категория;ед.измерения;тип. Найдены: "
                    + String.join(", ", header.keySet()));
            return new ParseResult(List.of(), errors);
        }

        List<NomenclatureCsvRow> rows = new ArrayList<>();
        Set<String> seenSkus = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            parseRow(line, i + 1, new int[]{nameIdx, skuIdx, categoryIdx, unitIdx, typeIdx, minStockIdx == null ? -1 : minStockIdx},
                    seenSkus, errors)
                    .ifPresent(rows::add);
        }
        return new ParseResult(rows, errors);
    }

    private Optional<NomenclatureCsvRow> parseRow(String line,
                                                  int lineNo,
                                                  int[] idx,
                                                  Set<String> seenSkus,
                                                  List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);
        String name = cell(cells, idx[0]);
        String sku = cell(cells, idx[1]);
        String categoryRaw = cell(cells, idx[2]);
        String unitRaw = cell(cells, idx[3]);
        String typeRaw = cell(cells, idx[4]);
        String minStockRaw = idx[5] >= 0 ? cell(cells, idx[5]) : "";

        if (name.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнено название");
            return Optional.empty();
        }
        if (sku.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен артикул");
            return Optional.empty();
        }
        if (!seenSkus.add(sku.toLowerCase())) {
            errors.add("Строка " + lineNo + ": позиция с артикулом «" + sku + "» встречается в файле дважды");
            return Optional.empty();
        }

        try {
            StuffCategory category = LabeledEnums.map(StuffCategory.class, categoryRaw, "категория");
            UnitOfMeasure unit = LabeledEnums.map(UnitOfMeasure.class, unitRaw, "ед. измерения");
            InventoryType type = LabeledEnums.map(InventoryType.class, typeRaw, "тип");
            BigDecimal minStock = parseMinStock(minStockRaw);
            return Optional.of(new NomenclatureCsvRow(lineNo, name, sku, category, unit, type, minStock));
        } catch (IllegalArgumentException e) {
            errors.add("Строка " + lineNo + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }

    private BigDecimal parseMinStock(String raw) {
        if (raw.isEmpty()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.replace(',', '.'));
            if (value.signum() < 0) {
                throw new IllegalArgumentException("Мин. количество не может быть отрицательным");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Не удалось прочитать мин. количество: «" + raw + "»");
        }
    }
}
