package com.example.WarehouseApp.controller.warehouse;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Controller
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final BaseService<WarehouseDto> warehouseService;
    private final BaseService<StorageLocationDto> storageLocationService;

    @GetMapping
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

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("warehouseDto", new WarehouseDto());
        return "warehouses/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouseDto", warehouseService.getById(id));
        return "warehouses/form";
    }

    @PostMapping
    public String save(@ModelAttribute @Valid WarehouseDto warehouseDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "warehouses/form";
        }
        if (warehouseDto.getId() != null) {
            warehouseService.update(warehouseDto);
        } else {
            warehouseService.create(warehouseDto);
        }
        return "redirect:/warehouse";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return "redirect:/warehouse";
    }

    @GetMapping("/{id}")
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

}
