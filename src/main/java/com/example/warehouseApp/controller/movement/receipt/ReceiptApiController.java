package com.example.warehouseApp.controller.movement.receipt;

import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.service.movement.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receipt/api")
@RequiredArgsConstructor
public class ReceiptApiController {

    private final ReceiptService receiptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDtoReceipt createReceipt(@RequestBody @Valid MovementDtoReceipt movementDtoReceipt) {
        return receiptService.createReceipt(movementDtoReceipt);
    }
}
