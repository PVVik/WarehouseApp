package com.example.warehouseApp.parser;

import java.util.HashMap;
import java.util.Map;

public final class CsvHeaders {

    private CsvHeaders() {
    }

    public static Map<String, Integer> parse(String headerLine) {
        Map<String, Integer> header = new HashMap<>();
        String[] cells = headerLine.split(";", -1);
        for (int i = 0; i < cells.length; i++) {
            header.put(cells[i].trim().toLowerCase(), i);
        }
        return header;
    }

    public static Integer find(Map<String, Integer> header, String... candidates) {
        for (String c : candidates) {
            if (header.containsKey(c)) {
                return header.get(c);
            }
        }
        return null;
    }
}