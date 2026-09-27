package com.example.WarehouseApp.controller;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("warehouses", warehouseService.getWarehouses());
        return "warehouses/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("warehouseDto", new WarehouseDto());
        return "warehouses/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("warehouseDto", warehouseService.getWarehouseById(id));
        return "warehouses/form";
    }

    @PostMapping
    public String save(@ModelAttribute @Valid WarehouseDto warehouseDto,
                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "warehouses/form";
        }
        if (warehouseDto.getId() != null) {
            warehouseService.updateWarehouse(warehouseDto);
        } else {
            warehouseService.addWarehouse(warehouseDto);
        }
        return "redirect:/warehouse";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return "redirect:/warehouse";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("warehouse", warehouseService.getWarehouseById(id));
        return "warehouses/view";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<WarehouseDto> getWarehousesApi() {
        return warehouseService.getWarehouses();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public WarehouseDto getWarehouseByIdApi(@PathVariable long id) {
        return warehouseService.getWarehouseById(id);
    }

    @PostMapping("/api")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseDto addWarehouseApi(@Valid @RequestBody WarehouseDto warehouseDto) {
        return warehouseService.addWarehouse(warehouseDto);
    }

    @PatchMapping("/api")
    @ResponseBody
    public WarehouseDto updateWarehouseApi(@RequestBody WarehouseDto warehouseDto) {
        return warehouseService.updateWarehouse(warehouseDto);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouseApi(@PathVariable long id) {
        warehouseService.deleteWarehouse(id);
    }
}
