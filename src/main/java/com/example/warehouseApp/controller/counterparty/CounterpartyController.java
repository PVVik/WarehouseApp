package com.example.warehouseApp.controller.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.model.CounterpartyType;
import com.example.warehouseApp.parser.counterparty.CounterpartyCsvParser;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.service.counterparty.CounterpartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/counterparty")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyService counterpartyService;
    private final CounterpartyCsvParser counterpartyCsvParser;

    @GetMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importForm() {
        return "counterparty/import";
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Выберите файл для загрузки");
            return "redirect:/counterparty/import";
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("error", "Поддерживаются только файлы .csv");
            return "redirect:/counterparty/import";
        }

        CounterpartyCsvParser.ParseResult parsed;
        try {
            parsed = counterpartyCsvParser.parse(file);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось прочитать файл. Попробуйте ещё раз");
            return "redirect:/counterparty/import";
        }

        if (parsed.rows().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Не удалось импортировать ни одной строки: "
                    + String.join("; ", parsed.errors()));
            return "redirect:/counterparty/import";
        }

        ImportResult result = counterpartyService.importAll(parsed.rows());

        String summary = "Создано контрагентов: " + result.created()
                + ", обновлено: " + result.updated() + ".";
        if (parsed.hasErrors()) {
            summary += " Пропущено строк с ошибками: " + parsed.errors().size() + ". "
                    + String.join("; ", parsed.errors());
            redirectAttributes.addFlashAttribute("error", summary);
        } else {
            redirectAttributes.addFlashAttribute("message", summary);
        }
        return "redirect:/counterparty";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String editForm(@PathVariable long id, Model model) {
        CounterpartyDto dto = counterpartyService.getById(id);
        model.addAttribute("counterpartyDto", dto);
        model.addAttribute("types", CounterpartyType.values());
        return "counterparty/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
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
    @PreAuthorize("hasRole('ADMIN')")
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


