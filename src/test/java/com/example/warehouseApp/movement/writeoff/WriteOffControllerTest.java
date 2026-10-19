package com.example.warehouseApp.movement.writeoff;

import com.example.warehouseApp.controller.movement.writeOff.WriteOffApiController;
import com.example.warehouseApp.dto.movement.MovementDtoWriteOff;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.service.movement.WriteOffService;
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

@WebMvcTest(controllers = WriteOffApiController.class)
class WriteOffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WriteOffService writeOffService;

    private static final Long VALID_STORAGE_LOCATION_ID = 4L;
    private static final Long VALID_WAREHOUSE_FROM_ID = 2L;
    private static final Long VALID_NOMENCLATURE_ID = 1L;
    private static final long VALID_USER_ID = 10L;

    @Test
    void createWriteOffApi_shouldReturn201_andJsonBody() throws Exception {
        var request = buildDto(
                null,
                MovementType.WRITE_OFF,
                VALID_NOMENCLATURE_ID,
                null, null,
                BigDecimal.valueOf(50),
                VALID_WAREHOUSE_FROM_ID,
                VALID_STORAGE_LOCATION_ID,
                "Списание по акту №123 (порча)",
                "WO-2026-000001",
                LocalDate.of(2026, 10, 6),
                VALID_USER_ID
        );

        var response = buildDto(
                100L,
                MovementType.WRITE_OFF,
                VALID_NOMENCLATURE_ID,
                null, null,
                BigDecimal.valueOf(50),
                VALID_WAREHOUSE_FROM_ID,
                VALID_STORAGE_LOCATION_ID,
                "Списание по акту №123 (порча)",
                "WO-2026-000001",
                LocalDate.of(2026, 10, 6),
                VALID_USER_ID
        );

        when(writeOffService.createWriteOff(any(MovementDtoWriteOff.class))).thenReturn(response);

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nomenclatureId").value(VALID_NOMENCLATURE_ID))
                .andExpect(jsonPath("$.warehouseFrom").value(VALID_WAREHOUSE_FROM_ID))
                .andExpect(jsonPath("$.storageLocationId").value(VALID_STORAGE_LOCATION_ID))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.notes").value("Списание по акту №123 (порча)"))
                .andExpect(jsonPath("$.movementType").value("WRITE_OFF"));

        verify(writeOffService).createWriteOff(any(MovementDtoWriteOff.class));
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenQuantityIsNegative() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, null,
                new BigDecimal("-5"), VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenQuantityIsZero() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, null,
                BigDecimal.ZERO, VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenNomenclatureIdIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, null, null, null,
                BigDecimal.TEN, VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenWarehouseFromIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, null,
                BigDecimal.TEN, null, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenStorageLocationIdIsNull() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, null,
                BigDecimal.TEN, VALID_WAREHOUSE_FROM_ID, null,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenNotesIsBlank() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, null,
                BigDecimal.TEN, VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "", "Num", LocalDate.now(), VALID_USER_ID
        );

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenNotesIsNull() throws Exception {
        var request = new MovementDtoWriteOff();
        request.setMovementType(MovementType.WRITE_OFF);
        request.setNomenclatureId(VALID_NOMENCLATURE_ID);
        request.setQuantity(BigDecimal.TEN);
        request.setWarehouseFrom(VALID_WAREHOUSE_FROM_ID);
        request.setStorageLocationId(VALID_STORAGE_LOCATION_ID);
        request.setUserId(VALID_USER_ID);

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService, never()).createWriteOff(any());
    }

    @Test
    void createWriteOffApi_shouldReturn404_whenNomenclatureNotFound() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, 999L, null, null,
                BigDecimal.TEN, VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        when(writeOffService.createWriteOff(any(MovementDtoWriteOff.class)))
                .thenThrow(new NotFoundException("Номенклатура с id 999 не найдена"));

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(writeOffService).createWriteOff(any(MovementDtoWriteOff.class));
    }

    @Test
    void createWriteOffApi_shouldReturn400_whenSerialItemQuantityNotOne() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, 99L, // serialItemId = 99
                new BigDecimal("2"), VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        when(writeOffService.createWriteOff(any(MovementDtoWriteOff.class)))
                .thenThrow(new ValidationException("Для серийной единицы количество должно быть равно 1"));

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(writeOffService).createWriteOff(any(MovementDtoWriteOff.class));
    }

    @Test
    void createWriteOffApi_shouldReturn500_whenSerialItemInvalidStatus() throws Exception {
        var request = buildDto(
                null, MovementType.WRITE_OFF, VALID_NOMENCLATURE_ID, null, 99L,
                BigDecimal.ONE, VALID_WAREHOUSE_FROM_ID, VALID_STORAGE_LOCATION_ID,
                "Причина списания", "Num", LocalDate.now(), VALID_USER_ID
        );

        when(writeOffService.createWriteOff(any(MovementDtoWriteOff.class)))
                .thenThrow(new IllegalStateException("Серийная единица не может быть списана: статус IN_USE"));

        mockMvc.perform(post("/writeoff/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(writeOffService).createWriteOff(any(MovementDtoWriteOff.class));
    }

    private MovementDtoWriteOff buildDto(Long id, MovementType movementType, Long nomenclatureId,
                                         Long batchId, Long serialItemId, BigDecimal quantity,
                                         Long warehouseFrom, Long storageLocationId,
                                         String notes, String number, LocalDate date, Long userId) {
        MovementDtoWriteOff dto = new MovementDtoWriteOff();
        dto.setId(id != null ? id : 0L);
        dto.setMovementType(movementType);
        dto.setNomenclatureId(nomenclatureId);
        dto.setBatchId(batchId);
        dto.setSerialItemId(serialItemId);
        dto.setQuantity(quantity);
        dto.setWarehouseFrom(warehouseFrom);
        dto.setStorageLocationId(storageLocationId);
        dto.setNotes(notes);
        dto.setNumber(number);
        dto.setDate(date);
        dto.setUserId(userId);
        return dto;
    }
}
