package com.example.WarehouseApp.controller.counterparty;

import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.model.CounterpartyType;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        model.addAttribute("types", CounterpartyType.values());

        return "counterparty/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) String from,
                             @RequestParam(required = false) String name,
                             Model model) {
        var dto = new CounterpartyDto();
        if (name != null && !name.isBlank()) {
            dto.setName(name);
        }
        model.addAttribute("counterpartyDto", dto);
        model.addAttribute("types", CounterpartyType.values());
        model.addAttribute("from", from);
        return "counterparty/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable long id, Model model) {
        CounterpartyDto dto = counterpartyService.getById(id);
        model.addAttribute("counterpartyDto", dto);
        model.addAttribute("types", CounterpartyType.values());
        return "counterparty/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("counterpartyDto") @Valid CounterpartyDto dto,
                       BindingResult result,
                       @RequestParam(required = false) String from,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("types", CounterpartyType.values());
            model.addAttribute("counterpartyDto", dto);
            return "counterparty/form";
        }

        if (dto.getId() == null) {
            counterpartyService.create(dto);
            redirectAttributes.addFlashAttribute("message", "Контрагент успешно добавлен");
        } else {
            counterpartyService.update(dto);
            redirectAttributes.addFlashAttribute("message", "Контрагент успешно обновлён");
        }

        if ("movements".equals(from)) {
            return "redirect:/movements/counterparty/list";
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
}


