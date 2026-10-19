package com.example.warehouseApp.controller;

import com.example.warehouseApp.service.analytics.ExpiryCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ExpiryCheckService expiryCheckService;

    @GetMapping("/")
    public String index(Model model) {
        // Получаем количество партий, которые истекают в течение 30 дней
        long expiringSoonCount = expiryCheckService.countExpiringSoon();

        // Кладём в модель под понятным именем
        model.addAttribute("expiringSoonCount", expiringSoonCount);

        return "index";
    }
}