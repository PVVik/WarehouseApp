package com.example.warehouseApp.controller.movement.issue;

import com.example.warehouseApp.dto.movement.MovementDtoIssue;
import com.example.warehouseApp.service.movement.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issue")
public class IssueApiController {

    private final IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementDtoIssue createIssue(@RequestBody @Valid MovementDtoIssue movementDtoIssue) {
        return issueService.createIssue(movementDtoIssue);
    }
}
