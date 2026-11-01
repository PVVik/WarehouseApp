package com.example.warehouseApp.serialItem;

import com.example.warehouseApp.controller.serialItem.SerialItemApiController;
import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.service.serialItem.SerialItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SerialItemApiController.class)
public class SerialItemApiControllerTest {

    @MockitoBean
    private SerialItemService serialItemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSerialItem_shouldReturn201_andJsonBody() throws Exception {
        Long nomenclatureId = 1L;

        SerialItemDto request = new SerialItemDto();
        request.setSerialNumber("SN-123456");
        request.setStatus(ItemStatus.IN_STOCK);
        request.setNotes("Тестовая серийная единица");
        request.setNomenclatureId(nomenclatureId);
        request.setPassportNumber("PSP-001");

        SerialItemDto response = buildDto(
                1L,
                "SN-123456",
                ItemStatus.IN_STOCK,
                "PSP-001",
                nomenclatureId,
                null,
                "Тестовая серийная единица"
        );

        when(serialItemService.create(any(SerialItemDto.class))).thenReturn(response);

        String jsonContent = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.serialNumber").value("SN-123456"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_STOCK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.passportNumber").value("PSP-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nomenclatureId").value(nomenclatureId));
    }

    @Test
    void createSerialItem_shouldReturn400_whenValidationFails() throws Exception {
        SerialItemDto request = new SerialItemDto();
        request.setSerialNumber("");
        request.setStatus(ItemStatus.IN_STOCK);
        request.setNotes("Тестовая серийная единица");
        request.setNomenclatureId(1L);
        request.setPassportNumber("PSP-001");

        String jsonContent = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
        org.mockito.Mockito.verify(serialItemService, org.mockito.Mockito.never())
                .create(ArgumentMatchers.any(SerialItemDto.class));
    }

    @Test
    void getSerialItemById_shouldReturn200_whenExists() throws Exception {
        long id = 1L;
        SerialItemDto dto = buildDto(
                id,
                "SN-654321",
                ItemStatus.IN_STOCK,
                "PSP-999",
                1L,
                3L,
                "Другая единица"
        );

        when(serialItemService.getById(id)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/serial-item/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.serialNumber").value("SN-654321"));
    }

    @Test
    void getSerialItemById_shouldReturn404_whenNotFound() throws Exception {
        long id = 999L;

        when(serialItemService.getById(id))
                .thenThrow(new com.example.warehouseApp.exception.NotFoundException(
                        String.format("Серийная единица с id %d не найдена", id)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/serial-item/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getAllSerialItems_shouldReturn200_andPage() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(
                        buildDto(1L, "SN-111", ItemStatus.IN_STOCK, "PSP-111", 1L, null, ""),
                        buildDto(2L, "SN-222", ItemStatus.IN_USE, "PSP-222", 2L, 4L, "")
                )
        );

        when(serialItemService.getAll(ArgumentMatchers.any(), ArgumentMatchers.eq(null)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/serial-item"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].serialNumber").value("SN-111"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].status").value("IN_USE"));
    }

    @Test
    void getAllSerialItems_withSearch_shouldReturn200() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(buildDto(3L, "SN-333", ItemStatus.IN_STOCK, "PSP-333", 3L, null, ""))
        );

        String search = "status:IN_STOCK,";

        when(serialItemService.getAll(ArgumentMatchers.any(), ArgumentMatchers.eq(search)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/serial-item").param("search", search))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1));
    }

    @Test
    void updateSerialItem_shouldReturn200_andUpdatedBody() throws Exception {
        SerialItemDto request = new SerialItemDto();
        request.setSerialNumber("");
        request.setStatus(ItemStatus.IN_STOCK);
        request.setNotes("Тестовая серийная единица");
        request.setNomenclatureId(1L);
        request.setPassportNumber("PSP-001");

        String jsonContent = objectMapper.writeValueAsString(request);

        SerialItemDto response = buildDto(
                1L,
                "SN-UPDATED",
                ItemStatus.IN_TRANSIT,
                "PSP-UPD",
                1L,
                5L,
                "Обновлено"
        );

        when(serialItemService.update(any(SerialItemDto.class))).thenReturn(response);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        );

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.serialNumber").value("SN-UPDATED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void deleteSerialItem_shouldReturn204_whenExists() throws Exception {
        long id = 1L;

        org.mockito.Mockito.doNothing().when(serialItemService).delete(id);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/serial-item/" + id))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        org.mockito.Mockito.verify(serialItemService).delete(org.mockito.ArgumentMatchers.eq(id));
    }

    @Test
    void deleteSerialItem_shouldReturn404_whenNotFound() throws Exception {
        long id = 999L;

        org.mockito.Mockito.doThrow(
                        new com.example.warehouseApp.exception.NotFoundException("Не найдено"))
                .when(serialItemService).delete(id);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/serial-item/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private SerialItemDto buildDto(Long id, String serialNumber, ItemStatus status,
                                   String passportNumber, Long nomenclatureId, Long batchId, String notes) {
        var dto = new SerialItemDto();
        dto.setId(id);
        dto.setSerialNumber(serialNumber);
        dto.setStatus(status);
        dto.setPassportNumber(passportNumber);
        dto.setNomenclatureId(nomenclatureId);
        dto.setBatchId(batchId);
        dto.setNotes(notes);
        return dto;
    }
}

