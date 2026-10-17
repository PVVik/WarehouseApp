package com.example.WarehouseApp.controller.movement.transfer;

import com.example.WarehouseApp.dto.movement.MovementDtoTransfer;
import com.example.WarehouseApp.service.movement.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transfer/api")
public class TransferApiController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDtoTransfer createTransfer(@Valid @RequestBody MovementDtoTransfer movementDtoTransfer) {
        return transferService.createTransfer(movementDtoTransfer);
    }
}
