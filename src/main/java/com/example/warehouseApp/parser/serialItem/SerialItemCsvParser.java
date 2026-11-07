package com.example.warehouseApp.parser.serialItem;

import com.example.warehouseApp.mapper.LabeledEnums;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.Nomenclature;
import com.example.warehouseApp.parser.CsvFiles;
import com.example.warehouseApp.parser.CsvHeaders;
import com.example.warehouseApp.repository.NomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SerialItemCsvParser {

    private static final String SEPARATOR = ";";

    private final NomenclatureRepository nomenclatureRepository;

    public record ParseResult(List<SerialItemCsvRow> rows, List<String> errors) {
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
        Integer serialIdx = CsvHeaders.find(header, "серийный номер", "серийник", "serialnumber");
        Integer statusIdx = CsvHeaders.find(header, "статус", "status");
        Integer passportIdx = CsvHeaders.find(header, "паспорт", "номер паспорта", "passport");
        Integer notesIdx = CsvHeaders.find(header, "заметки", "заметка", "примечание", "notes");

        if (nomenclatureIdx == null || serialIdx == null || statusIdx == null || passportIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: номенклатура;серийный номер;статус;паспорт;заметки. Найдены: "
                    + String.join(", ", header.keySet()));
            return new ParseResult(List.of(), errors);
        }

        List<SerialItemCsvRow> rows = new ArrayList<>();
        Set<String> seenSerials = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            parseRow(line, i + 1, nomenclatureIdx, serialIdx, statusIdx, passportIdx, notesIdx,
                    seenSerials, errors)
                    .ifPresent(rows::add);
        }
        return new ParseResult(rows, errors);
    }

    private Optional<SerialItemCsvRow> parseRow(String line,
                                                int lineNo,
                                                int nomenclatureIdx, int serialIdx, int statusIdx,
                                                int passportIdx, int notesIdx,
                                                Set<String> seenSerials,
                                                List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);
        String sku = cell(cells, nomenclatureIdx);
        String serialNumber = cell(cells, serialIdx);
        String statusRaw = cell(cells, statusIdx);
        String passport = cell(cells, passportIdx);
        String notes = notesIdx >= 0 ? cell(cells, notesIdx) : "";

        if (serialNumber.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен серийный номер");
            return Optional.empty();
        }
        if (!seenSerials.add(serialNumber.toLowerCase())) {
            errors.add("Строка " + lineNo + ": единица с серийным номером «" + serialNumber
                    + "» встречается в файле дважды");
            return Optional.empty();
        }
        if (passport.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен номер паспорта");
            return Optional.empty();
        }

        Nomenclature nomenclature = nomenclatureRepository.findBySkuIgnoreCase(sku).orElse(null);
        if (nomenclature == null) {
            errors.add("Строка " + lineNo + ": номенклатура с артикулом «" + sku + "» не найдена");
            return Optional.empty();
        }

        try {
            ItemStatus status = LabeledEnums.map(ItemStatus.class, statusRaw, "статус");
            return Optional.of(new SerialItemCsvRow(lineNo, nomenclature, serialNumber, status, passport,
                    notes.isEmpty() ? null : notes));
        } catch (IllegalArgumentException e) {
            errors.add("Строка " + lineNo + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }
}
