package com.example.warehouseApp.parser.stockBalance;

import com.example.warehouseApp.model.*;
import com.example.warehouseApp.parser.CsvFiles;
import com.example.warehouseApp.parser.CsvHeaders;
import com.example.warehouseApp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockBalanceCsvParser {

    private static final String SEPARATOR = ";";

    private final NomenclatureRepository nomenclatureRepository;
    private final BatchRepository batchRepository;
    private final SerialItemRepository serialItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository storageLocationRepository;

    public record ParseResult(List<StockBalanceCsvRow> rows, List<String> errors) {
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
        Integer batchIdx = CsvHeaders.find(header, "партия", "номер партии", "batch");
        Integer serialIdx = CsvHeaders.find(header, "серийная единица", "серийный номер", "серийник", "serial");
        Integer warehouseIdx = CsvHeaders.find(header, "склад", "warehouse");
        Integer locationIdx = CsvHeaders.find(header, "ячейка", "код ячейки", "location");
        Integer quantityIdx = CsvHeaders.find(header, "количество", "кол-во", "quantity");

        if (nomenclatureIdx == null || warehouseIdx == null || quantityIdx == null) {
            errors.add("Не найдены нужные колонки. Ожидались: номенклатура;партия;серийная единица;склад;ячейка;количество. Найдены: "
                    + String.join(", ", header.keySet()));
            return new ParseResult(List.of(), errors);
        }

        List<StockBalanceCsvRow> rows = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            parseRow(line, i + 1, nomenclatureIdx, batchIdx, serialIdx, warehouseIdx, locationIdx, quantityIdx,
                    seenKeys, errors)
                    .ifPresent(rows::add);
        }
        return new ParseResult(rows, errors);
    }

    private Optional<StockBalanceCsvRow> parseRow(String line,
                                                  int lineNo,
                                                  int nomenclatureIdx, int batchIdx, int serialIdx,
                                                  int warehouseIdx, int locationIdx, int quantityIdx,
                                                  Set<String> seenKeys,
                                                  List<String> errors) {
        String[] cells = line.split(SEPARATOR, -1);
        String sku = cell(cells, nomenclatureIdx);
        String batchNumber = batchIdx >= 0 ? cell(cells, batchIdx) : "";
        String serialNumber = serialIdx >= 0 ? cell(cells, serialIdx) : "";
        String warehouseName = cell(cells, warehouseIdx);
        String locationCode = locationIdx >= 0 ? cell(cells, locationIdx) : "";
        String quantityRaw = cell(cells, quantityIdx);

        if (sku.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнена номенклатура");
            return Optional.empty();
        }
        if (warehouseName.isEmpty()) {
            errors.add("Строка " + lineNo + ": не заполнен склад");
            return Optional.empty();
        }

        Nomenclature nomenclature = nomenclatureRepository.findBySkuIgnoreCase(sku).orElse(null);
        if (nomenclature == null) {
            errors.add("Строка " + lineNo + ": номенклатура с артикулом «" + sku + "» не найдена");
            return Optional.empty();
        }

        Warehouse warehouse = warehouseRepository.findByNameIgnoreCase(warehouseName).orElse(null);
        if (warehouse == null) {
            errors.add("Строка " + lineNo + ": склад «" + warehouseName + "» не найден");
            return Optional.empty();
        }

        Batch batch = null;
        if (!batchNumber.isEmpty()) {
            batch = batchRepository.findByBatchNumberIgnoreCase(batchNumber).orElse(null);
            if (batch == null) {
                errors.add("Строка " + lineNo + ": партия «" + batchNumber + "» не найдена");
                return Optional.empty();
            }
        }

        SerialItem serialItem = null;
        if (!serialNumber.isEmpty()) {
            serialItem = serialItemRepository.findBySerialNumberIgnoreCase(serialNumber).orElse(null);
            if (serialItem == null) {
                errors.add("Строка " + lineNo + ": серийная единица «" + serialNumber + "» не найдена");
                return Optional.empty();
            }
            if (batch == null && serialItem.getBatch() != null) {
                batch = serialItem.getBatch();
            }
        }

        StorageLocation storageLocation = null;
        if (!locationCode.isEmpty()) {
            storageLocation = storageLocationRepository
                    .findByWarehouseIdAndCodeIgnoreCase(warehouse.getId(), locationCode).orElse(null);
            if (storageLocation == null) {
                errors.add("Строка " + lineNo + ": ячейка «" + locationCode
                        + "» на складе «" + warehouseName + "» не найдена");
                return Optional.empty();
            }
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(quantityRaw.replace(',', '.'));
        } catch (NumberFormatException e) {
            errors.add("Строка " + lineNo + ": не удалось прочитать количество: «" + quantityRaw + "»");
            return Optional.empty();
        }
        if (quantity.signum() <= 0) {
            errors.add("Строка " + lineNo + ": количество должно быть больше нуля");
            return Optional.empty();
        }

        String key = String.join("|",
                sku.toLowerCase(),
                warehouseName.toLowerCase(),
                batchNumber.toLowerCase(),
                serialNumber.toLowerCase());
        if (!seenKeys.add(key)) {
            errors.add("Строка " + lineNo + ": остаток «" + sku + "» на складе «" + warehouseName
                    + "» с этой партией/серийником встречается в файле дважды");
            return Optional.empty();
        }

        return Optional.of(new StockBalanceCsvRow(lineNo, nomenclature, batch, serialItem,
                warehouse, storageLocation, quantity));
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }
}
