package com.example.warehouseApp.controller.movement.writeOff;

import com.example.warehouseApp.dto.movement.MovementDtoWriteOff;
import com.example.warehouseApp.service.movement.WriteOffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/writeoff/api")
public class WriteOffApiController {

    private final WriteOffService writeOffService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDtoWriteOff createIssue(@RequestBody @Valid MovementDtoWriteOff movementDtoWriteOff) {
        return writeOffService.createWriteOff(movementDtoWriteOff);
    }
}
