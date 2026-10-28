package com.example.warehouseApp.controller.domain;

import com.example.warehouseApp.domain.User;
import com.example.warehouseApp.dto.domain.CreateUserRequest;
import com.example.warehouseApp.dto.domain.UserResponseDto;
import com.example.warehouseApp.service.domain.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUserByAdmin(request);
        return ResponseEntity.ok(UserResponseDto.from(user));
    }
}