package com.example.WarehouseApp.movement.receipt;

import com.example.WarehouseApp.controller.movement.ReceiptApiController;
import com.example.WarehouseApp.dto.movement.MovementDtoReceipt;
import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.movement.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptApiController.class)
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReceiptService receiptService;

    @Test
    void createReceiptApi_shouldReturn201_andJsonBody() throws Exception {
        var request = buildDto(null, 1L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);
        var response = buildDto(100L, 1L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);
        response.setNumber("PRI-2026-000001");
        response.setDate(LocalDate.of(2026, 10, 6));

        when(receiptService.createReceipt(any(MovementDtoReceipt.class))).thenReturn(response);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nomenclatureId").value(1))
                .andExpect(jsonPath("$.warehouseTo").value(2))
                .andExpect(jsonPath("$.counterpartyId").value(3))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.price").value(120.50))
                .andExpect(jsonPath("$.number").value("PRI-2026-000001"))
                .andExpect(jsonPath("$.movementType").value("RECEIPT"));

        verify(receiptService).createReceipt(any(MovementDtoReceipt.class));
    }

    @Test
    void createReceiptApi_shouldReturn201_withBatch() throws Exception {
        var request = buildDto(null, 5L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("120"), new BigDecimal("9.90"), 10L);
        request.setBatchDto(buildBatchDto(null));

        var response = buildDto(101L, 5L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("120"), new BigDecimal("9.90"), 10L);
        response.setBatchDto(buildBatchDto(7L));
        response.setNumber("PRI-2026-000002");

        when(receiptService.createReceipt(any(MovementDtoReceipt.class))).thenReturn(response);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchDto.id").value(7))
                .andExpect(jsonPath("$.batchDto.batchNumber").value("BATCH-2026-001"));

        verify(receiptService).createReceipt(any(MovementDtoReceipt.class));
    }

    @Test
    void createReceiptApi_shouldReturn201_withSerialItem() throws Exception {
        var request = buildDto(null, 7L, MovementType.RECEIPT, 2L, 3L,
                BigDecimal.ONE, new BigDecimal("500000"), 10L);
        request.setSerialItemDto(buildSerialItemDto(null));

        var response = buildDto(102L, 7L, MovementType.RECEIPT, 2L, 3L,
                BigDecimal.ONE, new BigDecimal("500000"), 10L);
        response.setSerialItemDto(buildSerialItemDto(9L));
        response.setNumber("PRI-2026-000003");

        when(receiptService.createReceipt(any(MovementDtoReceipt.class))).thenReturn(response);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialItemDto.id").value(9))
                .andExpect(jsonPath("$.serialItemDto.serialNumber").value("УЭЦН-5A-001"));

        verify(receiptService).createReceipt(any(MovementDtoReceipt.class));
    }

    @Test
    void createReceiptApi_shouldReturn400_whenMovementTypeIsNull() throws Exception {
        var request = buildDto(null, 1L, null, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenNomenclatureIdIsNull() throws Exception {
        var request = buildDto(null, null, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenQuantityIsNegative() throws Exception {
        var request = buildDto(null, 1L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("-5"), new BigDecimal("120.50"), 10L);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenQuantityIsZero() throws Exception {
        var request = buildDto(null, 1L, MovementType.RECEIPT, 2L, 3L,
                BigDecimal.ZERO, new BigDecimal("120.50"), 10L);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenWarehouseToIsNull() throws Exception {
        var request = buildDto(null, 1L, MovementType.RECEIPT, null, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenUserIdIsNull() throws Exception {
        var request = buildDto(null, 1L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), null);

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn400_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(receiptService, never()).createReceipt(any());
    }

    @Test
    void createReceiptApi_shouldReturn404_whenNomenclatureNotFound() throws Exception {
        var request = buildDto(null, 999L, MovementType.RECEIPT, 2L, 3L,
                new BigDecimal("50"), new BigDecimal("120.50"), 10L);

        when(receiptService.createReceipt(any(MovementDtoReceipt.class)))
                .thenThrow(new NotFoundException("Номенклатура с id 999 не найдена"));

        mockMvc.perform(post("/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(receiptService).createReceipt(any(MovementDtoReceipt.class));
    }

    private MovementDtoReceipt buildDto(Long id, Long nomenclatureId, MovementType movementType,
                                        Long warehouseTo, Long counterpartyId,
                                        BigDecimal quantity, BigDecimal price, Long userId) {
        var dto = new MovementDtoReceipt(
                id != null ? id : 0L,
                movementType,
                nomenclatureId,
                warehouseTo,
                null,
                counterpartyId,
                quantity,
                price,
                null,
                LocalDate.of(2026, 10, 6),
                userId,
                "Приход по накладной №123");
        return dto;
    }

    private BatchDto buildBatchDto(Long id) {
        var dto = new BatchDto();
        if (id != null) {
            dto.setId(id);
        }
        dto.setNomenclatureId(5L);
        dto.setBatchNumber("BATCH-2026-001");
        dto.setSupplierId(3L);
        dto.setProductionDate(LocalDate.of(2026, 9, 1));
        dto.setExpiryDate(LocalDate.of(2028, 9, 1));
        dto.setCertificateNumber("CERT-777");
        dto.setReceiptDate(LocalDate.of(2026, 10, 6));
        return dto;
    }

    private SerialItemDto buildSerialItemDto(Long id) {
        var dto = new SerialItemDto();
        if (id != null) {
            dto.setId(id);
        }
        dto.setNomenclatureId(7L);
        dto.setSerialNumber("УЭЦН-5A-001");
        dto.setPassportNumber("П-778812");
        dto.setNotes("Новый насос");
        return dto;
    }
}