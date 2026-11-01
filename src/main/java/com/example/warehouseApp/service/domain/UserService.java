package com.example.warehouseApp.service.domain;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.domain.User;
import com.example.warehouseApp.dto.domain.CreateUserRequest;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    @LogAudit(action = "CREATE", entityType = User.class)
    public User createUserByAdmin(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Пользователь с логином '" + request.getUsername() + "' уже существует");
        }

        String tempPassword = RandomStringUtils.randomAlphanumeric(12);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(true);

        user.setPassword(passwordEncoder.encode(tempPassword));

        user = userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), tempPassword);

        return user;
    }
}