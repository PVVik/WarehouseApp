package com.example.warehouseApp.movement;

import com.example.warehouseApp.controller.movement.MovementApiController;
import com.example.warehouseApp.dto.movement.MovementResponseDto;
import com.example.warehouseApp.service.movement.MovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MovementApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovementApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovementService movementService;

    private Page<MovementResponseDto> samplePage;

    @BeforeEach
    void setUp() {
        var dto = new MovementResponseDto(
                LocalDate.of(2026, 10, 30),
                "DOC-001",
                "Приход",
                "Болт М8",
                "BATCH-123",
                BigDecimal.valueOf(10)
        );

        samplePage = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
    }

    @Test
    void getMovements_shouldReturn200AndPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").descending());

        when(movementService.getAll(pageable, null)).thenReturn(samplePage);

        mockMvc.perform(get("/api/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));

        verify(movementService).getAll(pageable, null);
    }

    @Test
    void getMovements_shouldApplySearchFilter() throws Exception {
        String search = "nomenclature:Болт";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").descending());

        when(movementService.getAll(pageable, search)).thenReturn(samplePage);

        mockMvc.perform(get("/api/movements")
                        .param("search", search))
                .andExpect(status().isOk());

        verify(movementService).getAll(pageable, search);
    }

    @Test
    void getMovements_shouldHandleMultipleSearchConditions() throws Exception {
        String search = "nomenclature:Болт,batch:BATCH";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").descending());

        when(movementService.getAll(pageable, search)).thenReturn(samplePage);

        mockMvc.perform(get("/api/movements")
                        .param("search", search))
                .andExpect(status().isOk());

        verify(movementService).getAll(pageable, search);
    }

    @Test
    void getMovements_shouldTreatEmptySearchAsNull() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").descending());

        when(movementService.getAll(pageable, "")).thenReturn(samplePage);

        mockMvc.perform(get("/api/movements")
                        .param("search", ""))
                .andExpect(status().isOk());

        verify(movementService).getAll(pageable, "");
    }

    @Test
    void getMovements_shouldTreatBlankSearchAsNull() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").descending());

        when(movementService.getAll(pageable, "   ")).thenReturn(samplePage);

        mockMvc.perform(get("/api/movements")
                        .param("search", "   "))
                .andExpect(status().isOk());

        verify(movementService).getAll(pageable, "   ");
    }
}
