package com.example.WarehouseApp.controller.storageLocation;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storageLocation/api")
@RequiredArgsConstructor
public class StorageLocationApiController {

    private final BaseService<StorageLocationDto> storageLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocationDto addStorageLocation(@RequestBody @Valid StorageLocationDto storageLocationDto) {
        return storageLocationService.create(storageLocationDto);
    }

    @PatchMapping
    public StorageLocationDto updateStorageLocation(@RequestBody StorageLocationDto storageLocationDto) {
        return storageLocationService.update(storageLocationDto);
    }

    @GetMapping("/{id}")
    public StorageLocationDto getStorageLocationById(@PathVariable long id) {
        return storageLocationService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStorageLocation(@PathVariable long id) {
        storageLocationService.delete(id);
    }

    @GetMapping
    public Page<StorageLocationDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                           @RequestParam(value = "search", required = false) String search) {
        return storageLocationService.getAll(pageable, search);
    }
}
