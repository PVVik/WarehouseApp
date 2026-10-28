package com.example.warehouseApp.controller.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
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
@RequestMapping("/api/counterparty")
@RequiredArgsConstructor
public class CounterpartyApiController {

    private final BaseService<CounterpartyDto> counterpartyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public CounterpartyDto addCounterparty(@RequestBody @Valid CounterpartyDto counterpartyDto) {
        return counterpartyService.create(counterpartyDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public CounterpartyDto getCounterpartyById(@PathVariable long id) {
        return counterpartyService.getById(id);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public CounterpartyDto updateCounterparty(@RequestBody CounterpartyDto counterpartyDto) {
        return counterpartyService.update(counterpartyDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public Page<CounterpartyDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return counterpartyService.getAll(pageable, search);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCounterparty(@PathVariable long id) {
        counterpartyService.delete(id);
    }
}
