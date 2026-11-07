package com.example.warehouseApp.parser.counterparty;

import com.example.warehouseApp.mapper.LabeledEnums;
import com.example.warehouseApp.model.CounterpartyType;
import com.example.warehouseApp.parser.CsvFiles;
import com.example.warehouseApp.parser.CsvHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class CounterpartyCsvParser {

    private static final String SEPARATOR = ";";

    public record ParseResult(List<CounterpartyCsvRow> rows, List<String> errors) {
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
        Integer nameIdx = CsvHeaders.find(header, "название организации", "название", "наименование", "name");
        Integer innIdx = CsvHeaders.find(header, "инн", "inn");
        Integer kppIdx = CsvHeaders.find(header, "кпп", "kpp");
        Integer typeIdx = CsvHeaders.find(header, "тип", "type");
        Integer phoneIdx = CsvHeaders.find(header, "номер телефона", "телефон", "phone");
        Integer contactIdx = CsvHeaders.find(header, "контактное лицо", "контакт", "contact");

        if (nameIdx == null || innIdx == null || kppIdx == null || typeIdx == null
                || phoneIdx == null || contactIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: название организации;инн;кпп;тип;номер телефона;контактное лицо. Найдены: "
                    + String.join(", ", header.keySet()));
            return new ParseResult(List.of(), errors);
        }

        List<CounterpartyCsvRow> rows = new ArrayList<>();
        Set<String> seenInns = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            parseRow(line, i + 1, nameIdx, innIdx, kppIdx, typeIdx, phoneIdx, contactIdx,
                    seenInns, errors)
                    .ifPresent(rows::add);
        }
        return new ParseResult(rows, errors);
    }

    private Optional<CounterpartyCsvRow> parseRow(String line,
                                                  int lineNo,
                                                  int nameIdx, int innIdx, int kppIdx,
                                                  int typeIdx, int phoneIdx, int contactIdx,
                                                  Set<String> seenInns,
                                                  List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);
        String name = cell(cells, nameIdx);
        String inn = cell(cells, innIdx);
        String kpp = cell(cells, kppIdx);
        String typeRaw = cell(cells, typeIdx);
        String phone = cell(cells, phoneIdx);
        String contact = cell(cells, contactIdx);

        if (name.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнено название организации");
            return Optional.empty();
        }
        if (!isValidInn(inn)) {
            errors.add("Строка " + lineNo + ": некорректный ИНН «" + inn
                    + "» — ожидаются 10 цифр (организация) или 12 (ИП)");
            return Optional.empty();
        }
        if (!seenInns.add(inn)) {
            errors.add("Строка " + lineNo + ": контрагент с ИНН «" + inn + "» встречается в файле дважды");
            return Optional.empty();
        }
        if (!isValidKpp(kpp)) {
            errors.add("Строка " + lineNo + ": некорректный КПП «" + kpp + "» — ожидаются 9 цифр");
            return Optional.empty();
        }
        if (phone.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен номер телефона");
            return Optional.empty();
        }
        if (contact.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнено контактное лицо");
            return Optional.empty();
        }

        try {
            CounterpartyType type = LabeledEnums.map(CounterpartyType.class, typeRaw, "тип");
            return Optional.of(new CounterpartyCsvRow(lineNo, name, inn, kpp, type, phone, contact));
        } catch (IllegalArgumentException e) {
            errors.add("Строка " + lineNo + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }

    private boolean isValidInn(String inn) {
        return inn.matches("\\d{10}") || inn.matches("\\d{12}");
    }

    private boolean isValidKpp(String kpp) {
        return kpp.matches("\\d{9}");
    }
}
