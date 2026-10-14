package com.example.WarehouseApp.movement.transfer;

import com.example.WarehouseApp.controller.movement.transfer.TransferApiController;
import com.example.WarehouseApp.dto.movement.MovementDtoTransfer;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.exception.ValidationException;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.service.movement.MovementTransferService;
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

@WebMvcTest(controllers = TransferApiController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovementTransferService movementTransferService;

    private static final Long VALID_STORAGE_LOCATION_ID = 4L;

    @Test
    void createTransferApi_shouldReturn201_andJsonBody() throws Exception {
        var request = buildDto(
                null,
                MovementType.TRANSFER,
                1L,
                2L,
                3L,
                VALID_STORAGE_LOCATION_ID,
                BigDecimal.valueOf(50),
                "TRF-2026-000001",
                LocalDate.of(2026, 10, 6),
                10L,
                "Перемещение между складами"
        );

        var response = buildDto(
                100L,
                MovementType.TRANSFER,
                1L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.valueOf(50),
                "TRF-2026-000001",
                LocalDate.of(2026, 10, 6),
                10L,
                "Перемещение между складами"
        );

        when(movementTransferService.createTransfer(any(MovementDtoTransfer.class))).thenReturn(response);

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nomenclatureId").value(1))
                .andExpect(jsonPath("$.warehouseFrom").value(2))
                .andExpect(jsonPath("$.warehouseTo").value(3))
                .andExpect(jsonPath("$.storageLocationId").value(4)) // Проверяем, что ячейка вернулась
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.number").value("TRF-2026-000001"))
                .andExpect(jsonPath("$.movementType").value("TRANSFER"));

        verify(movementTransferService).createTransfer(any(MovementDtoTransfer.class));
    }

    @Test
    void createTransferApi_shouldReturn400_whenQuantityIsNegative() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                new BigDecimal("-5"), null, null, 10L, null
        );

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenQuantityIsZero() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.ZERO, null, null, 10L, null
        );

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenNomenclatureIdIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                null, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.TEN, null, null, 10L, null
        );

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenWarehouseFromIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, null, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.TEN, null, null, 10L, null
        );

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenWarehouseToIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, 2L, null, VALID_STORAGE_LOCATION_ID,
                BigDecimal.TEN, null, null, 10L, null
        );

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenStorageLocationIdIsNull() throws Exception {
        var request = new MovementDtoTransfer();
        request.setMovementType(MovementType.TRANSFER);
        request.setNomenclatureId(1L);
        request.setWarehouseFrom(2L);
        request.setWarehouseTo(3L);
        request.setQuantity(BigDecimal.TEN);
        request.setUserId(10L);

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn400_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(movementTransferService, never()).createTransfer(any());
    }

    @Test
    void createTransferApi_shouldReturn404_whenNomenclatureNotFound() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                999L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.TEN, null, null, 10L, null
        );

        when(movementTransferService.createTransfer(any(MovementDtoTransfer.class)))
                .thenThrow(new NotFoundException("Номенклатура с id 999 не найдена"));

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(movementTransferService).createTransfer(any(MovementDtoTransfer.class));
    }

    @Test
    void createTransferApi_shouldReturn500_whenSerialItemNotInStock() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                BigDecimal.ONE, null, null, 10L, null
        );
        request.setSerialItemId(99L);

        when(movementTransferService.createTransfer(any(MovementDtoTransfer.class)))
                .thenThrow(new IllegalStateException("Серийная единица не может быть списана, статус — IN_USE"));

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(movementTransferService).createTransfer(any(MovementDtoTransfer.class));
    }

    @Test
    void createTransferApi_shouldReturn400_whenQuantityForSerialItemIsNotOne() throws Exception {
        var request = buildDto(
                null, MovementType.TRANSFER,
                1L, 2L, 3L, VALID_STORAGE_LOCATION_ID,
                new BigDecimal("2"), null, null, 10L, null
        );
        request.setSerialItemId(99L);

        when(movementTransferService.createTransfer(any(MovementDtoTransfer.class)))
                .thenThrow(new ValidationException("Проверьте введенное количество, для серийных единиц оно может быть только 1"));

        mockMvc.perform(post("/transfer/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(movementTransferService).createTransfer(any(MovementDtoTransfer.class));
    }

    private MovementDtoTransfer buildDto(Long id, MovementType movementType, Long nomenclatureId,
                                         Long warehouseFrom, Long warehouseTo, Long storageLocationId,
                                         BigDecimal quantity, String number, LocalDate date,
                                         long userId, String notes) {
        return new MovementDtoTransfer(
                id != null ? id : 0L,
                movementType,
                nomenclatureId,
                null,
                null,
                warehouseFrom,
                warehouseTo,
                storageLocationId,
                quantity,
                number,
                date,
                userId,
                notes
        );
    }
}