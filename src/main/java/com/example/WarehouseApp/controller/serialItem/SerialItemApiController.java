package com.example.WarehouseApp.controller.serialItem;

import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/serial-item/api")
public class SerialItemApiController {

    private final BaseService<SerialItemDto> serialItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SerialItemDto createSerialItem(@RequestBody @Valid SerialItemDto serialItemDto) {
        return serialItemService.create(serialItemDto);
    }

    @PatchMapping
    public SerialItemDto updateSerialItem(@RequestBody SerialItemDto serialItemDto) {
        return serialItemService.update(serialItemDto);
    }

    @GetMapping
    public Page<SerialItemDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                      @RequestParam(value = "search", required = false) String search) {
        return serialItemService.getAll(pageable, search);
    }

    @GetMapping("/{id}")
    public SerialItemDto getSerialItemById(@PathVariable long id) {
        return serialItemService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSerialItemById(@PathVariable long id) {
        serialItemService.delete(id);
    }
}
