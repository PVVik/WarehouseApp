package com.example.warehouseApp.parser;

import com.example.warehouseApp.model.WarehouseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class WarehouseCsvParser {

    private static final String SEPARATOR = ";";
    private static final String BOM = "\uFEFF";

    public record ParseResult(List<WarehouseCsvRow> rows, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public ParseResult parse(MultipartFile file) throws IOException {
        String content = removeBom(readContent(file));
        List<String> errors = new ArrayList<>();

        List<String> lines = splitLines(content);
        if (lines.isEmpty()) {
            errors.add("Файл пуст");
            return new ParseResult(List.of(), errors);
        }

        ColumnMapping mapping = ColumnMapping.of(lines.get(0), errors);
        if (mapping == null) {
            return new ParseResult(List.of(), errors);
        }

        List<WarehouseCsvRow> rows = parseRows(lines, mapping, errors);
        return new ParseResult(rows, errors);
    }

    private String readContent(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private String removeBom(String content) {
        return content.startsWith(BOM) ? content.substring(1) : content;
    }

    private List<String> splitLines(String content) {
        return Arrays.stream(content.split("\\r?\\n"))
                .toList();
    }

    private List<WarehouseCsvRow> parseRows(List<String> lines,
                                            ColumnMapping mapping,
                                            List<String> errors) {
        List<WarehouseCsvRow> rows = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            int lineNo = i + 1;

            parseRow(line, lineNo, mapping, seenNames, errors)
                    .ifPresent(rows::add);
        }
        return rows;
    }

    private Optional<WarehouseCsvRow> parseRow(String line,
                                               int lineNo,
                                               ColumnMapping mapping,
                                               Set<String> seenNames,
                                               List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);

        String name = mapping.name(cells);
        String address = mapping.address(cells);
        String typeRaw = mapping.type(cells);

        if (name.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнено название");
            return Optional.empty();
        }
        if (address.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен адрес");
            return Optional.empty();
        }
        if (!seenNames.add(name.toLowerCase())) {
            errors.add("Строка " + lineNo + ": склад «" + name + "» встречается в файле дважды");
            return Optional.empty();
        }

        try {
            return Optional.of(new WarehouseCsvRow(lineNo, name, address, mapType(typeRaw)));
        } catch (IllegalArgumentException e) {
            errors.add("Строка " + lineNo + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private WarehouseType mapType(String raw) {
        String value = raw.toLowerCase();
        if (value.startsWith("центр") || value.equals("central")) {
            return WarehouseType.CENTRAL;
        }
        if (value.startsWith("удал") || value.equals("remote")) {
            return WarehouseType.REMOTE;
        }
        throw new IllegalArgumentException("Неизвестный тип склада: «" + raw
                + "». Ожидался «Центральный» или «Удаленный»");
    }

    private Integer firstIndex(Map<String, Integer> header, String... candidates) {
        for (String c : candidates) {
            if (header.containsKey(c)) {
                return header.get(c);
            }
        }
        return null;
    }

    private String cell(String[] cells, int idx) {
        return idx < cells.length ? cells[idx].trim() : "";
    }

}

final class ColumnMapping {

    private final int nameIdx;
    private final int addressIdx;
    private final int typeIdx;

    private ColumnMapping(int nameIdx, int addressIdx, int typeIdx) {
        this.nameIdx = nameIdx;
        this.addressIdx = addressIdx;
        this.typeIdx = typeIdx;
    }

    static ColumnMapping of(String headerLine, List<String> errors) {
        Map<String, Integer> header = new HashMap<>();
        String[] cells = headerLine.split(";", -1);
        for (int i = 0; i < cells.length; i++) {
            header.put(cells[i].trim().toLowerCase(), i);
        }

        Integer nameIdx = firstIndex(header, "название", "наименование", "name");
        Integer addrIdx = firstIndex(header, "адрес", "address");
        Integer typeIdx = firstIndex(header, "тип", "type");

        if (nameIdx == null || addrIdx == null || typeIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: название;адрес;тип. Найдены: "
                    + String.join(", ", header.keySet()));
            return null;
        }
        return new ColumnMapping(nameIdx, addrIdx, typeIdx);
    }

    String name(String[] cells)   { return cell(cells, nameIdx); }
    String address(String[] cells) { return cell(cells, addressIdx); }
    String type(String[] cells)    { return cell(cells, typeIdx); }

    private static String cell(String[] cells, int idx) {
        return idx < cells.length ? cells[idx].trim() : "";
    }

    private static Integer firstIndex(Map<String, Integer> header, String... candidates) {
        for (String c : candidates) {
            if (header.containsKey(c)) {
                return header.get(c);
            }
        }
        return null;
    }
}

