package com.example.warehouseApp.domain;

import com.example.warehouseApp.dto.domain.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_db",
        "spring.datasource.username=warehouse_user",
        "spring.datasource.password=warehouse_pass",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateUserSuccessfully() throws Exception {
        prepareAdminUser();

        var request = new CreateUserRequest();
        request.setUsername("new-user-001");
        request.setEmail("new-user-001@test.com");
        request.setRole(User.Role.STOREKEEPER);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("new-user-001"))
                .andExpect(jsonPath("$.email").value("new-user-001@test.com"))
                .andExpect(jsonPath("$.role").value("STOREKEEPER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldFailWhenUsernameExists() throws Exception {
        prepareAdminUser();

        var firstRequest = new CreateUserRequest();
        firstRequest.setUsername("duplicate-user");
        firstRequest.setEmail("dup1@test.com");
        firstRequest.setRole(User.Role.VIEWER);

        String firstJson = objectMapper.writeValueAsString(firstRequest);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                .andExpect(status().isOk());

        var secondRequest = new CreateUserRequest();
        secondRequest.setUsername("duplicate-user");
        secondRequest.setEmail("dup2@test.com");
        secondRequest.setRole(User.Role.STOREKEEPER);

        String secondJson = objectMapper.writeValueAsString(secondRequest);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("уже существует")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldValidateRequestFields() throws Exception {
        prepareAdminUser();

        var request = new CreateUserRequest();
        request.setEmail("");
        request.setUsername("");
        request.setRole(User.Role.VIEWER);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldStoreHashedPasswordInDatabase() throws Exception {
        prepareAdminUser();

        var request = new CreateUserRequest();
        request.setUsername("hashed-test-user");
        request.setEmail("hashed@test.com");
        request.setRole(User.Role.ADMIN);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        String password = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE username = ?",
                String.class,
                "hashed-test-user"
        );

        assertNotNull(password);
        assertTrue(password.startsWith("$2a$"), "Пароль должен быть BCrypt-хэшем");
    }

    private void prepareAdminUser() throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
        jdbcTemplate.update("""
            INSERT INTO users (username, password, email, full_name, role, active)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                "admin",
                encodedPassword,
                "admin@test.com",
                "Admin User",
                "ADMIN",
                true);
    }

}
