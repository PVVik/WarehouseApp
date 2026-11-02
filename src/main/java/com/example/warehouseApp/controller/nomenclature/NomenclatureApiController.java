package com.example.warehouseApp.controller.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.parser.nomenclature.NomenclatureCsvParser;
import com.example.warehouseApp.parser.nomenclature.NomenclatureImportResult;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.nomenclature.NomenclatureService;
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
@RequestMapping("/api/nomenclature")
@RequiredArgsConstructor
public class NomenclatureApiController {

    private final NomenclatureService nomenclatureService;
    private final NomenclatureCsvParser nomenclatureCsvParser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public NomenclatureDto addNomenclature(@RequestBody @Valid NomenclatureDto nomenclatureDto) {
        return nomenclatureService.create(nomenclatureDto);
    }

    @PostMapping("/import.html")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public ResponseEntity<?> importCsvApi(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран или пуст"));
        }

        NomenclatureCsvParser.ParseResult parsed;
        try {
            parsed = nomenclatureCsvParser.parse(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Не удалось прочитать файл"));
        }

        if (parsed.rows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Не удалось импортировать ни одной строки",
                    "errors", parsed.errors()));
        }

        NomenclatureImportResult result = nomenclatureService.importAll(parsed.rows());

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
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public NomenclatureDto updateNomenclature(@RequestBody NomenclatureDto nomenclatureDto) {
        return nomenclatureService.update(nomenclatureDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<NomenclatureDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return nomenclatureService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public NomenclatureDto getNomenclatureById(@PathVariable long id) {
        return nomenclatureService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteNomenclature(@PathVariable long id) {
        nomenclatureService.delete(id);
    }
}
