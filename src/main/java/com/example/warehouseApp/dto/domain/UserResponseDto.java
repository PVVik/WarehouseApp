package com.example.warehouseApp.dto.domain;

import com.example.warehouseApp.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private User.Role role;
    private boolean active;

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}