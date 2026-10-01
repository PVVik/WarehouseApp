package com.example.WarehouseApp.controller;

import com.example.WarehouseApp.dto.NomenclatureDto;
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
@RequestMapping("/nomenclature")
@RequiredArgsConstructor
public class NomenclatureController {

    private final BaseService<NomenclatureDto> nomenclatureService;

    @GetMapping
    public String nomenclaturePage(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String sku,
                                   @RequestParam(required = false) String stuffCategory,
                                   @RequestParam(required = false) Boolean active,
                                   Model model) {

        var search = new StringBuilder();
        if (name != null && !name.isBlank()) {
            search.append("name:").append(name).append(",");
        }
        if (sku != null && !sku.isBlank()) {
            search.append("sku:").append(sku).append(",");
        }
        if (stuffCategory != null && !stuffCategory.isBlank()) {
            search.append("stuffCategory:").append(stuffCategory).append(",");
        }
        if (active != null) {
            search.append("isActive:").append(active).append(",");
        }

        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var result = nomenclatureService.getAll(pageable, search.toString());

        model.addAttribute("nomenclatureList", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("pageSize", result.getSize());

        model.addAttribute("filterName", name != null ? name : "");
        model.addAttribute("filterSku", sku != null ? sku : "");
        model.addAttribute("filterStuffCategory", stuffCategory != null ? stuffCategory : "");
        model.addAttribute("filterActive", active);

        return "nomenclature/list";
    }

    @GetMapping("/form")
    public String nomenclatureForm(@RequestParam(required = false) Long id, Model model) {

        NomenclatureDto dto;
        if (id != null) {
            dto = nomenclatureService.getById(id);
        } else {
            dto = new NomenclatureDto();
        }

        model.addAttribute("nomenclature", dto);
        model.addAttribute("isEditMode", id != null);

        return "nomenclature/form";
    }

    @PostMapping("/form")
    public String saveNomenclature(@ModelAttribute("nomenclature") @Valid NomenclatureDto dto,
                                   BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "nomenclature/form";
        }

        try {
            if (dto.getId() == null) {
                nomenclatureService.create(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Позиция успешно создана!");
            } else {
                nomenclatureService.update(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Позиция успешно обновлена!");
            }
            return "redirect:/nomenclature";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/nomenclature/form?id=" + dto.getId();
        }
    }

    @PostMapping("/api")
    @ResponseStatus(HttpStatus.CREATED)
    public NomenclatureDto addNomenclature(@RequestBody @Valid NomenclatureDto nomenclatureDto) {
        return nomenclatureService.create(nomenclatureDto);
    }

    @PatchMapping("/api")
    public NomenclatureDto updateNomenclature(@RequestBody NomenclatureDto nomenclatureDto) {
        return nomenclatureService.update(nomenclatureDto);
    }

    @GetMapping("/api")
    public Page<NomenclatureDto> getAll(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
                                        @RequestParam(value = "search", required = false) String search) {
        return nomenclatureService.getAll(pageable, search);
    }

    @GetMapping("/api/{id}")
    public NomenclatureDto getNomenclatureById(@PathVariable long id) {
        return nomenclatureService.getById(id);
    }

    @DeleteMapping("/api/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNomenclature(@PathVariable long id) {
        nomenclatureService.delete(id);
    }
}
