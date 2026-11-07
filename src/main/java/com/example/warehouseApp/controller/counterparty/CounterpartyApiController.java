package com.example.warehouseApp.controller.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.parser.counterparty.CounterpartyCsvParser;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.service.counterparty.CounterpartyService;
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
@RequestMapping("/api/counterparty")
@RequiredArgsConstructor
public class CounterpartyApiController {

    private final CounterpartyService counterpartyService;
    private final CounterpartyCsvParser counterpartyCsvParser;

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран или пуст"));
        }

        CounterpartyCsvParser.ParseResult parsed;
        try {
            parsed = counterpartyCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        ImportResult result = counterpartyService.importAll(parsed.rows());

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
    public CounterpartyDto addCounterparty(@RequestBody @Valid CounterpartyDto counterpartyDto) {
        return counterpartyService.create(counterpartyDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public CounterpartyDto getCounterpartyById(@PathVariable long id) {
        return counterpartyService.getById(id);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public CounterpartyDto updateCounterparty(@RequestBody CounterpartyDto counterpartyDto) {
        return counterpartyService.update(counterpartyDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<CounterpartyDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return counterpartyService.getAll(pageable, search);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCounterparty(@PathVariable long id) {
        counterpartyService.delete(id);
    }
}
