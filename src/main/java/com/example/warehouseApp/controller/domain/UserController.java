package com.example.warehouseApp.controller.domain;

import com.example.warehouseApp.dto.domain.CreateUserRequest;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.service.domain.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("userRequest", new CreateUserRequest());
        return "user/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveUser(@ModelAttribute("userRequest") @Valid CreateUserRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/form";
        }

        try {
            userService.createUserByAdmin(request);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь создан! На email отправлен временный пароль.");
            return "redirect:/";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании пользователя");
            return "redirect:/user/create";
        }
    }
}
