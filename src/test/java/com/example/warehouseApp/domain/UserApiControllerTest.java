package com.example.warehouseApp.domain;

import com.example.warehouseApp.controller.domain.UserApiController;
import com.example.warehouseApp.dto.domain.CreateUserRequest;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.service.domain.EmailService;
import com.example.warehouseApp.service.domain.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Test
    void createUserApi_shouldReturn201_andJsonBody() throws Exception {
        var request = new CreateUserRequest();
        request.setUsername("new-user-001");
        request.setEmail("new-user-001@test.com");
        request.setRole(User.Role.STOREKEEPER);

        var user = new User();
        user.setId(100L);
        user.setUsername(request.getUsername());
        user.setFullName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(true);

        when(userService.createUserByAdmin(any(CreateUserRequest.class)))
                .thenReturn(user);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.username").value("new-user-001"))
                .andExpect(jsonPath("$.email").value("new-user-001@test.com"))
                .andExpect(jsonPath("$.role").value("STOREKEEPER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).createUserByAdmin(any(CreateUserRequest.class));
    }

    @Test
    void createUserApi_shouldReturn400_whenUsernameIsBlank() throws Exception {
        var request = new CreateUserRequest();
        request.setUsername("");
        request.setEmail("test@test.com");
        request.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUserByAdmin(any());
        verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    void createUserApi_shouldReturn400_whenEmailIsInvalid() throws Exception {
        var request = new CreateUserRequest();
        request.setUsername("bad-email-user");
        request.setEmail("not-an-email");
        request.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUserByAdmin(any());
        verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    void createUserApi_shouldReturn400_whenUsernameExists() throws Exception {
        var request = new CreateUserRequest();
        request.setUsername("existing-user");
        request.setEmail("existing@test.com");
        request.setRole(User.Role.STOREKEEPER);

        when(userService.createUserByAdmin(any(CreateUserRequest.class)))
                .thenThrow(new BusinessException("Пользователь с логином 'existing-user' уже существует"));

        mockMvc.perform(post("/api/users/create")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).createUserByAdmin(any(CreateUserRequest.class));
        verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    void createUserApi_shouldNotSendEmail_whenServiceThrowsBeforeEmail() throws Exception {
        var request = new CreateUserRequest();
        request.setUsername("no-email-user");
        request.setEmail("no-email@test.com");
        request.setRole(User.Role.VIEWER);

        when(userService.createUserByAdmin(any(CreateUserRequest.class)))
                .thenThrow(new BusinessException("Ошибка до отправки письма"));

        mockMvc.perform(post("/api/users/create")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).createUserByAdmin(any(CreateUserRequest.class));
        verify(emailService, never()).sendWelcomeEmail(any(), any());
    }
}
