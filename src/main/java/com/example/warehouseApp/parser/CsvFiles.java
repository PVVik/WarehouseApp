package com.example.warehouseApp.parser;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class CsvFiles {

    private CsvFiles() {
    }

    private static final String BOM = "\uFEFF";

    public static List<String> readLines(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.startsWith(BOM)) {
            content = content.substring(1);
        }
        return Arrays.stream(content.split("\\r?\\n"))
                .toList();
    }
}
