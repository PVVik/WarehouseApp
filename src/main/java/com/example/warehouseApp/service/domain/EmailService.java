package com.example.warehouseApp.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendWelcomeEmail(String email, String tempPassword) {
        log.warn("=== ИМИТАЦИЯ EMAIL (НЕ ОТПРАВЛЯЕТСЯ РЕАЛЬНО) ====");
        log.warn("Кому: {}", email);
        log.warn("Временный пароль: {}", tempPassword);
        log.warn("================================================");
    }
}