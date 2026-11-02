package com.example.warehouseApp.parser.batch;

import com.example.warehouseApp.model.Counterparty;
import com.example.warehouseApp.model.Nomenclature;
import com.example.warehouseApp.parser.CsvFiles;
import com.example.warehouseApp.parser.CsvHeaders;
import com.example.warehouseApp.repository.CounterpartyRepository;
import com.example.warehouseApp.repository.NomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BatchCsvParser {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final NomenclatureRepository nomenclatureRepository;
    private final CounterpartyRepository counterpartyRepository;

    public record ParseResult(List<BatchCsvRow> rows, List<String> errors) {
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
        Integer nomenclatureIdx = CsvHeaders.find(header, "номенклатура", "артикул", "sku");
        Integer numberIdx = CsvHeaders.find(header, "номер партии", "партия", "batchnumber");
        Integer supplierIdx = CsvHeaders.find(header, "инн контрагента", "инн", "контрагент");
        Integer productionIdx = CsvHeaders.find(header, "дата изготовления", "дата производства");
        Integer expiryIdx = CsvHeaders.find(header, "срок годности");

        if (nomenclatureIdx == null || numberIdx == null || productionIdx == null || expiryIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: номенклатура;номер партии;контрагент;дата изготовления;срок годности. Найдены: "
                    + String.join(", ", header.keySet()));
            return new ParseResult(List.of(), errors);
        }

        List<BatchCsvRow> rows = new ArrayList<>();
        Set<String> seenNumbers = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            parseRow(line, i + 1,
                    nomenclatureIdx, numberIdx, supplierIdx, productionIdx, expiryIdx,
                    seenNumbers, errors)
                    .ifPresent(rows::add);
        }
        return new ParseResult(rows, errors);
    }

    private Optional<BatchCsvRow> parseRow(String line,
                                           int lineNo,
                                           int nomenclatureIdx, int numberIdx, int supplierIdx,
                                           int productionIdx, int expiryIdx,
                                           Set<String> seenNumbers,
                                           List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);
        String sku = cell(cells, nomenclatureIdx);
        String batchNumber = cell(cells, numberIdx);
        String supplierName = cell(cells, supplierIdx);
        String productionRaw = cell(cells, productionIdx);
        String expiryRaw = cell(cells, expiryIdx);

        if (batchNumber.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен номер партии");
            return Optional.empty();
        }
        if (!seenNumbers.add(batchNumber.toLowerCase())) {
            errors.add("Строка " + lineNo + ": партия с номером «" + batchNumber + "» встречается в файле дважды");
            return Optional.empty();
        }

        Nomenclature nomenclature = nomenclatureRepository.findBySkuIgnoreCase(sku).orElse(null);
        if (nomenclature == null) {
            errors.add("Строка " + lineNo + ": номенклатура с артикулом «" + sku + "» не найдена");
            return Optional.empty();
        }

        LocalDate productionDate = parseDate(productionRaw, "дата изготовления", lineNo, errors);
        LocalDate expiryDate = parseDate(expiryRaw, "срок годности", lineNo, errors);
        if (productionDate == null || expiryDate == null) {
            return Optional.empty();
        }
        if (productionDate.isAfter(LocalDate.now())) {
            errors.add("Строка " + lineNo + ": дата производства не может быть в будущем");
            return Optional.empty();
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            errors.add("Строка " + lineNo + ": срок годности не может быть в прошлом");
            return Optional.empty();
        }
        if (!expiryDate.isAfter(productionDate)) {
            errors.add("Строка " + lineNo + ": срок годности должен быть позже даты производства");
            return Optional.empty();
        }

        Counterparty supplier = null;
        if (!supplierName.isEmpty()) {
            supplier = counterpartyRepository.findByInn(supplierName).orElse(null);
            if (supplier == null) {
                errors.add("Строка " + lineNo + ": контрагент с ИНН «" + supplierName + "» не найден");
                return Optional.empty();
            }
        }

        return Optional.of(new BatchCsvRow(lineNo, nomenclature, batchNumber, supplier, productionDate, expiryDate));
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }

    private LocalDate parseDate(String raw, String fieldName, int lineNo, List<String> errors) {
        if (raw.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнена " + fieldName);
            return null;
        }
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                errors.add("Строка " + lineNo + ": не удалось прочитать «" + fieldName
                        + "»: «" + raw + "». Формат — дд.ММ.гггг");
                return null;
            }
        }
    }
}
