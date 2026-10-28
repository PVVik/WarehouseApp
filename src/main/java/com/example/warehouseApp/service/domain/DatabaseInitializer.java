package com.example.warehouseApp.service.domain;

import com.example.warehouseApp.domain.User;
import com.example.warehouseApp.domain.User.Role;
import com.example.warehouseApp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createInitialUsers();
    }

    private void createInitialUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("Главный Администратор");
            admin.setEmail("admin@warehouse.local");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            log.info("✅ Создан тестовый пользователь: admin");
        } else {
            log.info("⚠️ Пользователь 'admin' уже существует.");
        }

        if (userRepository.findByUsername("storekeeper").isEmpty()) {
            User sk = new User();
            sk.setUsername("storekeeper");
            sk.setFullName("Кладовщик");
            sk.setEmail("store@warehouse.local");
            sk.setRole(User.Role.STOREKEEPER);
            sk.setActive(true);
            sk.setPassword(passwordEncoder.encode("store123"));
            userRepository.save(sk);
            log.info("✅ Создан тестовый пользователь: storekeeper");
        }

        if (userRepository.findByUsername("viewer").isEmpty()) {
            User vw = new User();
            vw.setUsername("viewer");
            vw.setFullName("Аналитик");
            vw.setEmail("view@warehouse.local");
            vw.setRole(User.Role.VIEWER);
            vw.setActive(true);
            vw.setPassword(passwordEncoder.encode("view123"));
            userRepository.save(vw);
            log.info("✅ Создан тестовый пользователь: viewer");
        }
    }
}
