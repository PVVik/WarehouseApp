package com.example.warehouseApp.controller.batch;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.service.batch.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchApiController {

    private final BatchService batchService;

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
