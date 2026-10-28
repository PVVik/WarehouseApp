package com.example.warehouseApp.controller;

import com.example.warehouseApp.service.analytics.ExpiryCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ExpiryCheckService expiryCheckService;

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public String index(Model model) {
        long expiringSoonCount = expiryCheckService.countExpiringSoon();

        model.addAttribute("expiringSoonCount", expiringSoonCount);

        return "index";
    }
}