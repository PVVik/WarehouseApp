package com.example.warehouseApp.controller.serialItem;

import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.serialItem.SerialItemCsvParser;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/serial-item")
public class SerialItemApiController {

    private final SerialItemService serialItemService;
    private final SerialItemCsvParser serialItemCsvParser;

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран или пуст"));
        }

        SerialItemCsvParser.ParseResult parsed;
        try {
            parsed = serialItemCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        ImportResult result = serialItemService.importAll(parsed.rows());

        var body = new LinkedHashMap<String, Object>();
        body.put("created", result.created());
        body.put("updated", result.updated());
        body.put("imported", result.created() + result.updated());
        if (parsed.hasErrors()) {
            body.put("skipped", parsed.errors().size());
            body.put("errors", parsed.errors());
        }
        return ResponseEntity.ok(body);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public SerialItemDto createSerialItem(@RequestBody @Valid SerialItemDto serialItemDto) {
        return serialItemService.create(serialItemDto);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public SerialItemDto updateSerialItem(@RequestBody SerialItemDto serialItemDto) {
        return serialItemService.update(serialItemDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<SerialItemDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                      @RequestParam(value = "search", required = false) String search) {
        return serialItemService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public SerialItemDto getSerialItemById(@PathVariable long id) {
        return serialItemService.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSerialItemById(@PathVariable long id) {
        serialItemService.delete(id);
    }
}
