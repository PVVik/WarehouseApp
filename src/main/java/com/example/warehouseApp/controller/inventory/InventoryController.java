package com.example.warehouseApp.controller.inventory;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.inventory.*;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.inventory.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final BaseService<WarehouseDto> warehouseService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("inventoryForm", new InventoryCheckCreateDto());
        model.addAttribute("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
        return "inventory/inventory-form";
    }

    @PostMapping
    public String createCheck(@Valid @ModelAttribute("inventoryForm") InventoryCheckCreateDto dto,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("inventory/inventory-form");
            mav.addObject("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
            return mav.getViewName();
        }

        try {
            InventoryCheckDtoResponse response = inventoryService.createCheck(dto.getWarehouseId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Инвентаризация №" + response.getId() + " создана. Строк: " + response.getTotalLines());
            return "redirect:/inventory";
        } catch (BusinessException ex) {
            ModelAndView mav = new ModelAndView("inventory/inventory-form");
            mav.addObject("inventoryForm", dto);
            mav.addObject("error", ex.getMessage());
            mav.addObject("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
            return mav.getViewName();
        } catch (Exception ex) {
            ModelAndView mav = new ModelAndView("inventory/inventory-form");
            mav.addObject("inventoryForm", dto);
            mav.addObject("error", "Произошла внутренняя ошибка. Попробуйте позже.");
            mav.addObject("warehouses", warehouseService.getAll(Pageable.unpaged(), null));
            return mav.getViewName();
        }
    }

    @GetMapping
    public String listInventory(Model model) {
        List<InventoryCheckDtoResponse> list = inventoryService.getAllChecks();
        model.addAttribute("checks", list);
        return "inventory/inventory-list";
    }

    @GetMapping("/{id}/lines")
    public String showLines(@PathVariable Long id, Model model) {
        InventoryCheckDtoResponse check = inventoryService.getById(id);
        List<InventoryLineDto> lines = inventoryService.getLinesByCheckId(id);

        List<LineInputItem> items = lines.stream()
                .map(line -> new LineInputItem(line.getId(), line.getActualQty()))
                .toList();

        LineInputForm form = new LineInputForm(items);

        model.addAttribute("check", check);
        model.addAttribute("lines", lines);
        model.addAttribute("lineInput", form);
        return "inventory/inventory-lines";
    }

    @PostMapping("/{id}/lines")
    public String saveActualQuantities(@PathVariable Long id,
                                       @ModelAttribute("lineInput") LineInputForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<InventoryLineDto> linesForDisplay = inventoryService.getLinesByCheckId(id);
            InventoryCheckDtoResponse check = inventoryService.getById(id);

            ModelAndView mav = new ModelAndView("inventory/inventory-lines");
            mav.addObject("check", check);
            mav.addObject("lines", linesForDisplay);
            mav.addObject("lineInput", form);
            mav.addObject("error", "Проверьте введённые значения");
            return mav.getViewName();
        }

        try {
            for (LineInputItem item : form.getItems()) {
                if (item.getActualQty() != null) {
                    inventoryService.updateActualQty(item.getId(), item.getActualQty());
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Фактические количества успешно сохранены!");
            return "redirect:/inventory/" + id + "/lines";
        } catch (BusinessException | IllegalArgumentException ex) {
            List<InventoryLineDto> linesForDisplay = inventoryService.getLinesByCheckId(id);
            InventoryCheckDtoResponse check = inventoryService.getById(id);

            ModelAndView mav = new ModelAndView("inventory/inventory-lines");
            mav.addObject("check", check);
            mav.addObject("lines", linesForDisplay);
            mav.addObject("lineInput", form);
            mav.addObject("error", ex.getMessage());
            return mav.getViewName();
        } catch (Exception ex) {
            List<InventoryLineDto> linesForDisplay = inventoryService.getLinesByCheckId(id);
            InventoryCheckDtoResponse check = inventoryService.getById(id);

            ModelAndView mav = new ModelAndView("inventory/inventory-lines");
            mav.addObject("check", check);
            mav.addObject("lines", linesForDisplay);
            mav.addObject("lineInput", form);
            mav.addObject("error", "Произошла внутренняя ошибка. Попробуйте позже.");
            return mav.getViewName();
        }
    }

    @PostMapping("/{id}/complete")
    public String completeCheck(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            inventoryService.completeCheck(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Инвентаризация №" + id + " успешно завершена. Акт расхождений сформирован."
            );
            return "redirect:/inventory";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventory/" + id;
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Инвентаризация не найдена");
            return "redirect:/inventory";
        }
    }

    @GetMapping("/{id}/review")
    public String reviewCheck(@PathVariable Long id, Model model) {

        InventoryCheckDtoResponse dto = inventoryService.getById(id);
        List<InventoryLineDto> lines = inventoryService.getLinesByCheckId(id);

        model.addAttribute("check", dto);
        model.addAttribute("lines", lines);
        model.addAttribute("isReviewMode", true);

        return "inventory/check-review";
    }
}