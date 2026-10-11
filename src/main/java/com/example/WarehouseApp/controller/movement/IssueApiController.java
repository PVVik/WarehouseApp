package com.example.WarehouseApp.controller.movement;

import com.example.WarehouseApp.dto.movement.MovementDtoIssue;
import com.example.WarehouseApp.service.movement.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/issue")
public class IssueApiController {

    private final IssueService issueService;

    @PostMapping
    public MovementDtoIssue createIssue(@RequestBody MovementDtoIssue movementDtoIssue) {
        return issueService.createIssue(movementDtoIssue);
    }
}
