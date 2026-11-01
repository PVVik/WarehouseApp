package com.example.warehouseApp.controller.analytics.expiryControl;

import com.example.warehouseApp.dto.analytics.ExpiryControlItemDto;
import com.example.warehouseApp.service.analytics.ExpiryControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/expiry-control")
@RequiredArgsConstructor
public class ExpiryControlApiController {

    private final ExpiryControlService expiryControlService;

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ADMIN', 'STOREKEEPER')")
    public Page<ExpiryControlItemDto> getExpiryControlApi(
            @PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable,
            @RequestParam(value = "search", required = false) String search) {
        return expiryControlService.getControlList(pageable, search);
    }
}
