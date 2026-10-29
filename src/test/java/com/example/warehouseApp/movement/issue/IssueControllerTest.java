package com.example.warehouseApp.movement.issue;

import com.example.warehouseApp.controller.movement.issue.IssueApiController;
import com.example.warehouseApp.dto.movement.MovementDtoIssue;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.service.movement.IssueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IssueApiController.class)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueService issueService;

    @Test
    void createIssueApi_shouldReturn201_andJsonBody() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), "ISS-2026-000001", LocalDate.of(2026, 10, 6));
        var response = buildDto(100L, MovementType.ISSUE, 1L, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), "ISS-2026-000001", LocalDate.of(2026, 10, 6));

        when(issueService.createIssue(any(MovementDtoIssue.class))).thenReturn(response);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nomenclatureId").value(1))
                .andExpect(jsonPath("$.warehouseFrom").value(2))
                .andExpect(jsonPath("$.customerId").value(3))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.price").value(120.50))
                .andExpect(jsonPath("$.number").value("ISS-2026-000001"))
                .andExpect(jsonPath("$.movementType").value("ISSUE"));

        verify(issueService).createIssue(any(MovementDtoIssue.class));
    }

    @Test
    void createIssueApi_shouldReturn400_whenQuantityIsNegative() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, 3L,
                new BigDecimal("-5"), new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenQuantityIsZero() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, 3L,
                BigDecimal.ZERO, new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenNomenclatureIdIsNull() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, null, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenWarehouseFromIsNull() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, null, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenCustomerIdIsNull() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, null,
                new BigDecimal("50"), new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenUserIdIsNull() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), null, null);

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn400_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssueApi_shouldReturn404_whenNomenclatureNotFound() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 999L, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), null, null);

        when(issueService.createIssue(any(MovementDtoIssue.class)))
                .thenThrow(new NotFoundException("Номенклатура с id 999 не найдена"));

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(issueService).createIssue(any(MovementDtoIssue.class));
    }

    @Test
    void createIssueApi_shouldReturn500_whenSerialItemNotInStock() throws Exception {
        var request = buildDto(null, MovementType.ISSUE, 1L, 2L, 3L,
                BigDecimal.ONE, new BigDecimal("5000"), null, null);
        request.setSerialItemId(99L);

        when(issueService.createIssue(any(MovementDtoIssue.class)))
                .thenThrow(new IllegalStateException("Серийная единица не может быть списана, статус — IN_USE"));

        mockMvc.perform(post("/issue/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(issueService).createIssue(any(MovementDtoIssue.class));
    }

    private MovementDtoIssue buildDto(Long id, MovementType movementType, Long nomenclatureId, Long warehouseFrom,
                                      Long customerId, BigDecimal quantity, BigDecimal price,
                                      String number, LocalDate date) {
        return new MovementDtoIssue(
                id != null ? id : 0L,
                movementType,
                nomenclatureId,
                warehouseFrom,
                customerId,
                quantity,
                price,
                number,
                date,
                "Отгрузка по накладной №123"
        );
    }
}

