package com.example.WarehouseApp.controller.storageLocation;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/storageLocation")
@RequiredArgsConstructor
public class StorageLocationController {

    private final BaseService<StorageLocationDto> storageLocationService;

    @GetMapping("/create")
    public String createForm(@RequestParam Long warehouseId, Model model) {
        StorageLocationDto dto = new StorageLocationDto();
        dto.setWarehouseId(warehouseId);
        dto.setUsed(false);
        model.addAttribute("storageLocationDto", dto);
        return "storageLocation/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable long id, Model model) {
        model.addAttribute("storageLocationDto", storageLocationService.getById(id));
        return "storageLocation/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("storageLocationDto") @Valid StorageLocationDto dto,
                       BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "storageLocation/form";
        }
        if (dto.getId() == null) {
            storageLocationService.create(dto);
            redirectAttributes.addFlashAttribute("message", "Ячейка создана");
        } else {
            storageLocationService.update(dto);
            redirectAttributes.addFlashAttribute("message", "Ячейка обновлена");
        }
        return "redirect:/warehouse/" + dto.getWarehouseId();
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable long id, @RequestParam Long warehouseId,
                         RedirectAttributes redirectAttributes) {
        storageLocationService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Ячейка удалена");
        return "redirect:/warehouse/" + warehouseId;
    }

}
