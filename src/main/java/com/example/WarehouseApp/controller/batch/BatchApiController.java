package com.example.WarehouseApp.controller.batch;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batch/api")
@RequiredArgsConstructor
public class BatchApiController {

    private final BaseService<BatchDto> batchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchDto addBatch(@RequestBody @Valid BatchDto batchDto) {
        return batchService.create(batchDto);
    }

    @PatchMapping
    public BatchDto updateBatch(@RequestBody BatchDto batchDto) {
        return batchService.update(batchDto);
    }

    @GetMapping
    public Page<BatchDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                 @RequestParam(value = "search", required = false) String search) {
        return batchService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    public BatchDto getBatchById(@PathVariable long id) {
        return batchService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatchById(@PathVariable long id) {
        batchService.delete(id);
    }
}
