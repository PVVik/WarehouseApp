package com.example.WarehouseApp.controller.warehouse;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse/api")
@RequiredArgsConstructor
public class WarehouseApiController {

    private final BaseService<WarehouseDto> warehouseService;

    @GetMapping
    @ResponseBody
    public Page<WarehouseDto> getWarehousesApi(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                               @RequestParam(value = "search", required = false) String search) {
        return warehouseService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public WarehouseDto getWarehouseByIdApi(@PathVariable long id) {
        return warehouseService.getById(id);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseDto addWarehouseApi(@Valid @RequestBody WarehouseDto warehouseDto) {
        return warehouseService.create(warehouseDto);
    }

    @PatchMapping
    @ResponseBody
    public WarehouseDto updateWarehouseApi(@RequestBody WarehouseDto warehouseDto) {
        return warehouseService.update(warehouseDto);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouseApi(@PathVariable long id) {
        warehouseService.delete(id);
    }
}
