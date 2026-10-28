package com.example.warehouseApp.controller.storageLocation;

import com.example.warehouseApp.dto.StorageLocationDto;
import com.example.warehouseApp.service.storageLocation.StorageLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storageLocation")
@RequiredArgsConstructor
public class StorageLocationApiController {

    private final StorageLocationService storageLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public StorageLocationDto addStorageLocation(@RequestBody @Valid StorageLocationDto storageLocationDto) {
        return storageLocationService.create(storageLocationDto);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public StorageLocationDto updateStorageLocation(@RequestBody StorageLocationDto storageLocationDto) {
        return storageLocationService.update(storageLocationDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public StorageLocationDto getStorageLocationById(@PathVariable long id) {
        return storageLocationService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteStorageLocation(@PathVariable long id) {
        storageLocationService.delete(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public Page<StorageLocationDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                           @RequestParam(value = "search", required = false) String search) {
        return storageLocationService.getAll(pageable, search);
    }
}
