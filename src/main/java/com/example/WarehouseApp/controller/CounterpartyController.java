package com.example.WarehouseApp.controller;

import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.model.CounterpartyType;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/counterparty")
@RequiredArgsConstructor
public class CounterpartyController {

    private final BaseService<CounterpartyDto> counterpartyService;

    @GetMapping
    public String listPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String inn,
                           @RequestParam(required = false) String counterpartyType,
                           Model model) {

        var search = new StringBuilder();
        if (name != null && !name.isBlank()) {
            search.append("name:").append(name).append(",");
        }
        if (inn != null && !inn.isBlank()) {
            search.append("inn:").append(inn).append(",");
        }
        if (counterpartyType != null && !counterpartyType.isBlank()) {
            search.append("counterpartyType:").append(counterpartyType).append(",");
        }

        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        var result = counterpartyService.getAll(pageable, search.toString());

        model.addAttribute("counterparties", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("pageSize", result.getSize());

        model.addAttribute("filterName", name != null ? name : "");
        model.addAttribute("filterInn", inn != null ? inn : "");
        model.addAttribute("filterType", counterpartyType != null ? counterpartyType : "");

        model.addAttribute("counterpartyTypes", CounterpartyType.values());

        return "counterparty/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("counterpartyDto", new CounterpartyDto());
        model.addAttribute("types", java.util.Arrays.asList(CounterpartyType.values()));
        return "counterparty/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable long id, Model model) {
        CounterpartyDto dto = counterpartyService.getById(id);
        model.addAttribute("counterpartyDto", dto);
        model.addAttribute("types", java.util.Arrays.asList(CounterpartyType.values()));
        return "counterparty/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("counterpartyDto") @Valid CounterpartyDto dto,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "counterparty/form";
        }
        if (dto.getId() == null) {
            counterpartyService.create(dto);
            redirectAttributes.addFlashAttribute("message", "Контрагент успешно добавлен");
        } else {
            counterpartyService.update(dto);
            redirectAttributes.addFlashAttribute("message", "Контрагент успешно обновлён");
        }
        return "redirect:/counterparty";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            counterpartyService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Контрагент удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить контрагента");
        }
        return "redirect:/counterparty";
    }

    @PostMapping("/api")
    @ResponseStatus(HttpStatus.CREATED)
    public CounterpartyDto addCounterparty(@RequestBody @Valid CounterpartyDto counterpartyDto) {
        return counterpartyService.create(counterpartyDto);
    }

    @GetMapping("/api/{id}")
    public CounterpartyDto getCounterpartyById(@PathVariable long id) {
        return counterpartyService.getById(id);
    }

    @PatchMapping("/api")
    public CounterpartyDto updateCounterparty(@RequestBody CounterpartyDto counterpartyDto) {
        return counterpartyService.update(counterpartyDto);
    }

    @GetMapping("/api")
    public Page<CounterpartyDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return counterpartyService.getAll(pageable, search);
    }

    @DeleteMapping("/api/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCounterparty(@PathVariable long id) {
        counterpartyService.delete(id);
    }
}
