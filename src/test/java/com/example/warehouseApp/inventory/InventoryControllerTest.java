package com.example.warehouseApp.inventory;

import com.example.warehouseApp.controller.inventory.InventoryApiController;
import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.model.DocumentStatus;
import com.example.warehouseApp.service.inventory.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryApiController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void createCheck_shouldReturn201_andJsonBody() throws Exception {
        Long warehouseId = 123L;
        LocalDate checkDate = LocalDate.of(2026, 10, 22);

        InventoryCheckDtoResponse expectedResponse = new InventoryCheckDtoResponse(
                100L,
                warehouseId,
                checkDate,
                DocumentStatus.DRAFT
        );
        expectedResponse.setTotalLines(5L);

        when(inventoryService.createCheck(eq(warehouseId))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", warehouseId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.warehouseId").value(123))
                .andExpect(jsonPath("$.checkDate").value("2026-10-22"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalLines").value(5));

        verify(inventoryService).createCheck(eq(warehouseId));
    }

    @Test
    void createCheck_shouldReturn400_whenWarehouseIdIsNull() throws Exception {
        // @RequestParam без значения → параметр не передан → Spring может выбросить MissingServletRequestParameterException
        // В тестах проще проверить, что метод сервиса вообще не вызван и статус 400
        mockMvc.perform(post("/api/inventory/checks"))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).createCheck(anyLong());
    }

    @Test
    void updateActualQty_shouldReturn200_andUpdatedLine() throws Exception {
        Long lineId = 55L;
        BigDecimal qty = new BigDecimal("100.50");

        InventoryLineDto expectedLine = new InventoryLineDto(
                lineId,
                "Товар А",
                "B2024",
                new BigDecimal("95.00"),
                qty
        );

        doNothing().when(inventoryService).updateActualQty(eq(lineId), eq(qty));
        when(inventoryService.getLineById(eq(lineId))).thenReturn(expectedLine);

        mockMvc.perform(put("/api/inventory/lines/{lineId}/actual-qty", lineId)
                        .param("qty", qty.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.nomenclatureName").value("Товар А"))
                .andExpect(jsonPath("$.accountedQty").value(95.00))
                .andExpect(jsonPath("$.actualQty").value(100.50));

        verify(inventoryService).updateActualQty(eq(lineId), eq(qty));
        verify(inventoryService).getLineById(eq(lineId));
    }

    @Test
    void updateActualQty_shouldReturn400_whenQtyIsNull() throws Exception {
        Long lineId = 55L;

        mockMvc.perform(put("/api/inventory/lines/{lineId}/actual-qty", lineId))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).updateActualQty(anyLong(), any());
        verify(inventoryService, never()).getLineById(anyLong());
    }

    @Test
    void updateActualQty_shouldReturn400_whenQtyIsNegative() throws Exception {
        Long lineId = 55L;
        BigDecimal negativeQty = new BigDecimal("-10");

        doThrow(new IllegalArgumentException("actualQty не может быть null или отрицательным"))
                .when(inventoryService).updateActualQty(eq(lineId), eq(negativeQty));

        mockMvc.perform(put("/api/inventory/lines/{lineId}/actual-qty", lineId)
                        .param("qty", negativeQty.toString()))
                .andExpect(status().isBadRequest());

        verify(inventoryService).updateActualQty(eq(lineId), eq(negativeQty));
        verify(inventoryService, never()).getLineById(anyLong());
    }

    @Test
    void updateActualQty_shouldReturn404_whenLineNotFound() throws Exception {
        Long lineId = 999L;
        BigDecimal qty = new BigDecimal("10");

        doThrow(new NotFoundException("Строка инвентаризации с id 999 не найдена"))
                .when(inventoryService).updateActualQty(eq(lineId), eq(qty));

        mockMvc.perform(put("/api/inventory/lines/{lineId}/actual-qty", lineId)
                        .param("qty", qty.toString()))
                .andExpect(status().isNotFound());

        verify(inventoryService).updateActualQty(eq(lineId), eq(qty));
        verify(inventoryService, never()).getLineById(anyLong());
    }

    @Test
    void completeCheck_shouldReturn200_andActDto() throws Exception {
        Long checkId = 200L;

        InventoryDiscrepancyActDto expectedAct = new InventoryDiscrepancyActDto(
                777L,
                checkId,
                "COMPLETED",
                1234L,
                DiscrepancyStatus.DRAFT,
                LocalDateTime.now(),
                null,
                2L,
                Collections.emptyList()
        );

        when(inventoryService.completeCheck(eq(checkId))).thenReturn(expectedAct);

        mockMvc.perform(post("/api/inventory/checks/{checkId}/complete", checkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(777))
                .andExpect(jsonPath("$.checkId").value(200))
                .andExpect(jsonPath("$.checkStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.totalLines").value(2));

        verify(inventoryService).completeCheck(eq(checkId));
    }

    @Test
    void completeCheck_shouldReturn404_whenCheckNotFound() throws Exception {
        Long checkId = 999L;

        doThrow(new NotFoundException("Инвентаризация не найдена"))
                .when(inventoryService).completeCheck(eq(checkId));

        mockMvc.perform(post("/api/inventory/checks/{checkId}/complete", checkId))
                .andExpect(status().isNotFound());

        verify(inventoryService).completeCheck(eq(checkId));
    }

    @Test
    void completeCheck_shouldReturn400_whenBusinessException() throws Exception {
        Long checkId = 200L;

        doThrow(new BusinessException("Есть строки без фактического количества"))
                .when(inventoryService).completeCheck(eq(checkId));

        mockMvc.perform(post("/api/inventory/checks/{checkId}/complete", checkId))
                .andExpect(status().isConflict());

        verify(inventoryService).completeCheck(eq(checkId));
    }

    @Test
    void getAllChecks_shouldReturnList() throws Exception {
        List<InventoryCheckDtoResponse> checks = List.of(
                new InventoryCheckDtoResponse(1L, 10L, LocalDate.now(), DocumentStatus.DRAFT),
                new InventoryCheckDtoResponse(2L, 20L, LocalDate.now().minusDays(1), DocumentStatus.COMPLETED)
        );

        when(inventoryService.getAllChecks()).thenReturn(checks);

        mockMvc.perform(get("/api/inventory/checks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[*].warehouseId").isArray());

        verify(inventoryService).getAllChecks();
    }

    @Test
    void getCheckById_shouldReturnSingleCheck() throws Exception {
        Long id = 1L;
        InventoryCheckDtoResponse check = new InventoryCheckDtoResponse(id, 10L, LocalDate.now(), DocumentStatus.IN_PROGRESS);

        when(inventoryService.getById(eq(id))).thenReturn(check);

        mockMvc.perform(get("/api/inventory/checks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(inventoryService).getById(eq(id));
    }

    @Test
    void getCheckById_shouldReturn404_whenNotFound() throws Exception {
        Long id = 999L;

        doThrow(new NotFoundException("Инвентаризации с id 999 не существует"))
                .when(inventoryService).getById(eq(id));

        mockMvc.perform(get("/api/inventory/checks/{id}", id))
                .andExpect(status().isNotFound());

        verify(inventoryService).getById(eq(id));
    }

    @Test
    void getLinesByCheckId_shouldReturnLinesList() throws Exception {
        Long checkId = 1L;
        List<InventoryLineDto> lines = List.of(
                new InventoryLineDto(101L, "Товар 1", new BigDecimal("50"), new BigDecimal("45")),
                new InventoryLineDto(102L, "Товар 2", new BigDecimal("20"), new BigDecimal("22"))
        );

        when(inventoryService.getLinesByCheckId(eq(checkId))).thenReturn(lines);

        mockMvc.perform(get("/api/inventory/checks/{checkId}/lines", checkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[1].nomenclatureName").value("Товар 2"))
                .andExpect(jsonPath("$[*].accountedQty").isArray());

        verify(inventoryService).getLinesByCheckId(eq(checkId));
    }
}
