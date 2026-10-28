package com.example.warehouseApp.controller.movement.transfer;

import com.example.warehouseApp.dto.movement.MovementDtoTransfer;
import com.example.warehouseApp.service.movement.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transfer")
public class TransferApiController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'STOREKEEPER')")
    public MovementDtoTransfer createTransfer(@Valid @RequestBody MovementDtoTransfer movementDtoTransfer) {
        return transferService.createTransfer(movementDtoTransfer);
    }
}
