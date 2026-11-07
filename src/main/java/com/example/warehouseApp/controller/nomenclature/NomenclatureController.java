package com.example.warehouseApp.controller.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.nomenclature.NomenclatureCsvParser;
import com.example.warehouseApp.service.nomenclature.NomenclatureService;
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
@RequestMapping("/nomenclature")
@RequiredArgsConstructor
public class NomenclatureController {

    private final NomenclatureService nomenclatureService;
    private final NomenclatureCsvParser nomenclatureCsvParser;

    @GetMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importForm() {
        return "nomenclature/import.html";
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Выберите файл для загрузки");
            return "redirect:/nomenclature/import";
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("error", "Поддерживаются только файлы .csv");
            return "redirect:/nomenclature/import";
        }

        NomenclatureCsvParser.ParseResult parsed;
        try {
            parsed = nomenclatureCsvParser.parse(file);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось прочитать файл. Попробуйте ещё раз");
            return "redirect:/nomenclature/import";
        }

        if (parsed.rows().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Не удалось импортировать ни одной строки: "
                    + String.join("; ", parsed.errors()));
            return "redirect:/nomenclature/import";
        }

        ImportResult result = nomenclatureService.importAll(parsed.rows());

        String summary = "Создано позиций: " + result.created()
                + ", обновлено: " + result.updated() + ".";
        if (parsed.hasErrors()) {
            summary += " Пропущено строк с ошибками: " + parsed.errors().size() + ". "
                    + String.join("; ", parsed.errors());
            redirectAttributes.addFlashAttribute("error", summary);
        } else {
            redirectAttributes.addFlashAttribute("message", summary);
        }
        return "redirect:/nomenclature";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
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
        model.addAttribute("from", from);

        return "nomenclature/form";
    }

    @PostMapping("/form")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
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
