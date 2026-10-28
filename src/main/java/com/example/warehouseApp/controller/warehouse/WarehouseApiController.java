package com.example.warehouseApp.controller.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
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
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseApiController {

    private final BaseService<WarehouseDto> warehouseService;

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public Page<WarehouseDto> getWarehousesApi(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                               @RequestParam(value = "search", required = false) String search) {
        return warehouseService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
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
