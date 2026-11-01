package com.example.warehouseApp.controller.serialItem;

import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/serial-item")
public class SerialItemApiController {

    private final SerialItemService serialItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public SerialItemDto createSerialItem(@RequestBody @Valid SerialItemDto serialItemDto) {
        return serialItemService.create(serialItemDto);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public SerialItemDto updateSerialItem(@RequestBody SerialItemDto serialItemDto) {
        return serialItemService.update(serialItemDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<SerialItemDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                      @RequestParam(value = "search", required = false) String search) {
        return serialItemService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public SerialItemDto getSerialItemById(@PathVariable long id) {
        return serialItemService.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSerialItemById(@PathVariable long id) {
        serialItemService.delete(id);
    }
}
