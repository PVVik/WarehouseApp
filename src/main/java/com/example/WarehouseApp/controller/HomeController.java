package com.example.WarehouseApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Thymeleaf автоматически добавит .html и найдёт в templates/
    }
}