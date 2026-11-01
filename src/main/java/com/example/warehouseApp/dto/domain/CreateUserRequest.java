package com.example.warehouseApp.dto.domain;

import com.example.warehouseApp.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Логин обязателен")
    private String username;

    @Email(message = "Некорректный email")
    private String email;

    @NotNull(message = "Роль обязательна")
    private User.Role role;
}
