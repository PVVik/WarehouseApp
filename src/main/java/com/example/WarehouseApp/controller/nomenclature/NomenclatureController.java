package com.example.WarehouseApp.controller.nomenclature;

import com.example.WarehouseApp.dto.NomenclatureDto;
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
    public String nomenclatureForm(@RequestParam(required = false) Long id,
                                   @RequestParam(required = false) String from,
                                   @RequestParam(required = false) String name,
                                   Model model) {

        NomenclatureDto dto;
        if (id != null) {
            dto = nomenclatureService.getById(id);
        } else {
            dto = new NomenclatureDto();
            if (name != null && !name.isBlank()) {
                dto.setName(name);
            }
        }

        model.addAttribute("nomenclature", dto);
        model.addAttribute("isEditMode", id != null);
        model.addAttribute("from", from); // запоминаем, откуда пришли

        return "nomenclature/form";
    }


    @PostMapping("/form")
    public String saveNomenclature(@ModelAttribute("nomenclature") @Valid NomenclatureDto dto,
                                   BindingResult result,
                                   @RequestParam(required = false) String from,
                                   RedirectAttributes redirectAttributes) {
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

            if ("movements".equals(from)) {
                return "redirect:/movements/nomenclature/list";
            }
            return "redirect:/nomenclature";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/nomenclature/form?id=" + dto.getId() + (from != null ? "&from=" + from : "");
        }
    }

}
