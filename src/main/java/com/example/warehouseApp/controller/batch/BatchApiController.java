package com.example.warehouseApp.controller.batch;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.batch.BatchCsvParser;
import com.example.warehouseApp.service.batch.BatchService;
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
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchApiController {

    private final BatchService batchService;
    private final BatchCsvParser batchCsvParser;

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран или пуст"));
        }

        BatchCsvParser.ParseResult parsed;
        try {
            parsed = batchCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        ImportResult result = batchService.importAll(parsed.rows());

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
    public BatchDto addBatch(@RequestBody @Valid BatchDto batchDto) {
        return batchService.create(batchDto);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public BatchDto updateBatch(@RequestBody BatchDto batchDto) {
        return batchService.update(batchDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<BatchDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                 @RequestParam(value = "search", required = false) String search) {
        return batchService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public BatchDto getBatchById(@PathVariable long id) {
        return batchService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBatchById(@PathVariable long id) {
        batchService.delete(id);
    }
}
