package com.example.WarehouseApp.controller.counterparty;

import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counterparty/api")
@RequiredArgsConstructor
public class CounterpartyApiController {

    private final BaseService<CounterpartyDto> counterpartyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CounterpartyDto addCounterparty(@RequestBody @Valid CounterpartyDto counterpartyDto) {
        return counterpartyService.create(counterpartyDto);
    }

    @GetMapping("/{id}")
    public CounterpartyDto getCounterpartyById(@PathVariable long id) {
        return counterpartyService.getById(id);
    }

    @PatchMapping
    public CounterpartyDto updateCounterparty(@RequestBody CounterpartyDto counterpartyDto) {
        return counterpartyService.update(counterpartyDto);
    }

    @GetMapping
    public Page<CounterpartyDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return counterpartyService.getAll(pageable, search);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCounterparty(@PathVariable long id) {
        counterpartyService.delete(id);
    }
}
