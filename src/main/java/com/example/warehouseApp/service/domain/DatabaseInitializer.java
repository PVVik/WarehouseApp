package com.example.warehouseApp.service.domain;

import com.example.warehouseApp.domain.User;
import com.example.warehouseApp.domain.User.Role;
import com.example.warehouseApp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Главный Администратор");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println("✅ Создан пользователь: admin / admin123");
        } else {
            System.out.println("⚠️ Пользователь 'admin' уже существует.");
        }

        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("man123"));
            manager.setFullName("Администратор");
            manager.setRole(Role.MANAGER);
            manager.setActive(true);

            userRepository.save(manager);
            System.out.println("✅ Создан пользователь: manager / man123");
        }

        if (userRepository.findByUsername("viewer").isEmpty()) {
            User viewer = new User();
            viewer.setUsername("viewer");
            viewer.setPassword(passwordEncoder.encode("viewer123"));
            viewer.setFullName("просмотр");
            viewer.setRole(Role.VIEWER);
            viewer.setActive(true);

            userRepository.save(viewer);
            System.out.println("✅ Создан пользователь: viewer / viewer123");
        }
    }
}