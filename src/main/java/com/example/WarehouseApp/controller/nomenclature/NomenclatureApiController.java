package com.example.WarehouseApp.controller.nomenclature;

import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomenclature/api")
@RequiredArgsConstructor
public class NomenclatureApiController {

    private final BaseService<NomenclatureDto> nomenclatureService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NomenclatureDto addNomenclature(@RequestBody @Valid NomenclatureDto nomenclatureDto) {
        return nomenclatureService.create(nomenclatureDto);
    }

    @PatchMapping
    public NomenclatureDto updateNomenclature(@RequestBody NomenclatureDto nomenclatureDto) {
        return nomenclatureService.update(nomenclatureDto);
    }

    @GetMapping
    public Page<NomenclatureDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return nomenclatureService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    public NomenclatureDto getNomenclatureById(@PathVariable long id) {
        return nomenclatureService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNomenclature(@PathVariable long id) {
        nomenclatureService.delete(id);
    }
}
