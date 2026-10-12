package com.example.WarehouseApp.controller.movement.issue;

import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.service.movement.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/issue/api")
public class IssueApiController {

    private final IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDtoIssue createIssue(@RequestBody @Valid MovementDtoIssue movementDtoIssue) {
        return issueService.createIssue(movementDtoIssue);
    }
}
