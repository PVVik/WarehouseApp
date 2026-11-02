package com.example.warehouseApp.controller.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.parser.WarehouseCsvParser;
import com.example.warehouseApp.parser.WarehouseImportResult;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.warehouse.WarehouseService;
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
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseApiController {

    private final WarehouseService warehouseService;
    private final WarehouseCsvParser warehouseCsvParser;

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<WarehouseDto> getWarehousesApi(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                               @RequestParam(value = "search", required = false) String search) {
        return warehouseService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public WarehouseDto getWarehouseByIdApi(@PathVariable long id) {
        return warehouseService.getById(id);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public WarehouseDto addWarehouseApi(@Valid @RequestBody WarehouseDto warehouseDto) {
        return warehouseService.create(warehouseDto);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Файл не выбран или пуст"));
        }

        WarehouseCsvParser.ParseResult parsed;
        try {
            parsed = warehouseCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        WarehouseImportResult result = warehouseService.importAll(parsed.rows());

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

    @PatchMapping
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public WarehouseDto updateWarehouseApi(@RequestBody WarehouseDto warehouseDto) {
        return warehouseService.update(warehouseDto);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteWarehouseApi(@PathVariable long id) {
        warehouseService.delete(id);
    }
}
