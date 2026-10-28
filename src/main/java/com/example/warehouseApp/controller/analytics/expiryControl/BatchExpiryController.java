package com.example.warehouseApp.controller.analytics.expiryControl;

import com.example.warehouseApp.dto.analytics.ExpiryControlItemDto;
import com.example.warehouseApp.service.analytics.ExpiryControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class BatchExpiryController {

    private final ExpiryControlService expiryControlService;

    @GetMapping("/expiry-control")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER', 'VIEWER')")
    public String expiryControl(Model model, Pageable pageable,
                                @RequestParam(required = false) String search) {
        Page<ExpiryControlItemDto> page = expiryControlService.getControlList(pageable, search);
        model.addAttribute("batches", page);

        return "analytics/expiry-control";
    }
}