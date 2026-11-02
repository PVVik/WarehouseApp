package com.example.warehouseApp.controller.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.parser.warehouse.WarehouseCsvParser;
import com.example.warehouseApp.parser.warehouse.WarehouseImportResult;
import com.example.warehouseApp.service.storageLocation.StorageLocationService;
import com.example.warehouseApp.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final StorageLocationService storageLocationService;
    private final WarehouseCsvParser warehouseCsvParser;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public String list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name, Model model) {

        var search = new StringBuilder();
        if (name != null && !name.isBlank()) {
            search.append("name:").append(name).append(",");
        }

        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        var result = warehouseService.getAll(pageable, search.toString());

        model.addAttribute("warehouses", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("pageSize", result.getSize());

        model.addAttribute("filterName", name != null ? name : "");

        return "warehouses/list";
    }

    @GetMapping("/import.html")
    @PreAuthorize("hasRole('ADMIN')")
    public String importForm() {
        return "warehouses/import.html";
    }

    @PostMapping("/import.html")
    @PreAuthorize("hasRole('ADMIN')")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Выберите файл для загрузки");
            return "redirect:/warehouses/import.html";
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("error", "Поддерживаются только файлы .csv");
            return "redirect:/warehouses/import.html";
        }

        WarehouseCsvParser.ParseResult parsed;
        try {
            parsed = warehouseCsvParser.parse(file);
        } catch (IOException e) {
            log.error("Не удалось прочитать файл импорта складов", e);
            redirectAttributes.addFlashAttribute("error", "Не удалось прочитать файл. Попробуйте ещё раз");
            return "redirect:/warehouses/import.html";
        }

        if (parsed.rows().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Не удалось импортировать ни одной строки: "
                    + String.join("; ", parsed.errors()));
            return "redirect:/warehouses/import.html";
        }

        WarehouseImportResult result = warehouseService.importAll(parsed.rows());

        String summary = "Создано складов: " + result.created()
                + ", обновлено: " + result.updated() + ".";
        if (parsed.hasErrors()) {
            summary += " Пропущено строк с ошибками: " + parsed.errors().size() + ". "
                    + String.join("; ", parsed.errors());
            redirectAttributes.addFlashAttribute("error", summary);
        } else {
            redirectAttributes.addFlashAttribute("message", summary);
        }
        return "redirect:/warehouse";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(@RequestParam(required = false) String from,
                             @RequestParam(required = false) String name,
                             Model model) {
        var dto = new WarehouseDto();
        if (name != null && !name.isBlank()) {
            dto.setName(name);
        }
        model.addAttribute("warehouseDto", dto);
        model.addAttribute("from", from);
        return "warehouses/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouseDto", warehouseService.getById(id));
        return "warehouses/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String save(@ModelAttribute @Valid WarehouseDto warehouseDto,
                       BindingResult bindingResult,
                       @RequestParam(required = false) String from) {
        if (bindingResult.hasErrors()) {
            return "warehouses/form";
        }
        if (warehouseDto.getId() != null) {
            warehouseService.update(warehouseDto);
        } else {
            warehouseService.create(warehouseDto);
        }

        if ("movements".equals(from)) {
            return "redirect:/movements/warehouse/list";
        }
        return "redirect:/warehouse";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return "redirect:/warehouse";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public String viewWarehouse(@PathVariable long id,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String zone,
                                @RequestParam(required = false) String rack,
                                @RequestParam(required = false) String shelf,
                                @RequestParam(required = false) Boolean used,
                                Model model) {

        model.addAttribute("warehouse", warehouseService.getById(id));

        StringBuilder search = new StringBuilder();
        search.append("warehouseId:").append(id).append(",");
        if (zone != null && !zone.isBlank()) {
            search.append("zone:").append(zone).append(",");
        }
        if (rack != null && !rack.isBlank()) {
            search.append("rack:").append(rack).append(",");
        }
        if (shelf != null && !shelf.isBlank()) {
            search.append("shelf:").append(shelf).append(",");
        }
        if (used != null) {
            search.append("used:").append(used).append(",");
        }

        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        var result = storageLocationService.getAll(pageable, search.toString());

        model.addAttribute("storageLocations", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("pageSize", result.getSize());

        model.addAttribute("filterZone", zone != null ? zone : "");
        model.addAttribute("filterRack", rack != null ? rack : "");
        model.addAttribute("filterShelf", shelf != null ? shelf : "");
        model.addAttribute("filterUsed", used);

        return "warehouses/view";
    }

    @GetMapping("/{id}/locations/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String bulkLocationsForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouse", warehouseService.getById(id));
        return "warehouses/bulk-locations";
    }

    @PostMapping("/{id}/locations/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public String bulkLocationsCreate(@PathVariable Long id,
                                      @RequestParam String zone,
                                      @RequestParam String rack,
                                      @RequestParam(defaultValue = "10") int count,
                                      RedirectAttributes redirectAttributes) {

        var result = storageLocationService.createBulk(id, zone, rack, count);

        if (!result.success()) {
            redirectAttributes.addFlashAttribute("error", result.message());
            return "redirect:/warehouse/" + id;
        }

        redirectAttributes.addFlashAttribute("message", result.message());
        return "redirect:/warehouse/" + id;
    }
}
