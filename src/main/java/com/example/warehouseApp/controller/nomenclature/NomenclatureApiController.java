package com.example.warehouseApp.controller.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nomenclature")
@RequiredArgsConstructor
public class NomenclatureApiController {

    private final BaseService<NomenclatureDto> nomenclatureService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public NomenclatureDto addNomenclature(@RequestBody @Valid NomenclatureDto nomenclatureDto) {
        return nomenclatureService.create(nomenclatureDto);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public NomenclatureDto updateNomenclature(@RequestBody NomenclatureDto nomenclatureDto) {
        return nomenclatureService.update(nomenclatureDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public Page<NomenclatureDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return nomenclatureService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
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
