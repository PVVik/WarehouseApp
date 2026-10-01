package com.example.WarehouseApp.storageLocation;

import com.example.WarehouseApp.controller.StorageLocationController;
import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.service.BaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorageLocationController.class)
class StorageLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaseService<StorageLocationDto> storageLocationService;

    private StorageLocationDto buildDto(Long id, long warehouseId, String zone, String rack, String shelf, boolean used) {
        StorageLocationDto dto = new StorageLocationDto();
        dto.setId(id);
        dto.setWarehouseId(warehouseId);
        dto.setZone(zone);
        dto.setRack(rack);
        dto.setShelf(shelf);
        dto.setUsed(used);
        dto.setCode(String.format("%s-%s-%s", zone, rack, shelf));
        dto.setWarehouseName("Склад 1");
        return dto;
    }

    @Test
    void addStorageLocationApi_shouldReturn201_andJsonBody() throws Exception {
        StorageLocationDto request = buildDto(null, 1L, "A", "01", "02", false);
        StorageLocationDto response = buildDto(1L, 1L, "A", "01", "02", false);

        when(storageLocationService.create(any(StorageLocationDto.class))).thenReturn(response);

        mockMvc.perform(post("/storageLocation/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.zone").value("A"))
                .andExpect(jsonPath("$.rack").value("01"))
                .andExpect(jsonPath("$.shelf").value("02"))
                .andExpect(jsonPath("$.code").value("A-01-02"))
                .andExpect(jsonPath("$.used").value(false))
                .andExpect(jsonPath("$.warehouseName").value("Склад 1"));
    }

    @Test
    void addStorageLocationApi_shouldReturn400_whenZoneIsBlank() throws Exception {
        StorageLocationDto request = buildDto(null, 1L, "", "01", "02", false);

        mockMvc.perform(post("/storageLocation/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storageLocationService, never()).create(any());
    }

    @Test
    void getStorageLocationByIdApi_shouldReturn200_whenExists() throws Exception {
        StorageLocationDto dto = buildDto(1L, 1L, "A", "01", "02", true);

        when(storageLocationService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/storageLocation/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.zone").value("A"))
                .andExpect(jsonPath("$.code").value("A-01-02"))
                .andExpect(jsonPath("$.used").value(true));
    }

    @Test
    void getStorageLocationByIdApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        when(storageLocationService.getById(id))
                .thenThrow(new NotFoundException(String.format("Ячейка с id %d не найдена", id)));

        mockMvc.perform(get("/storageLocation/api/" + id))
                .andExpect(status().isNotFound());

        verify(storageLocationService).getById(eq(id));
    }

    @Test
    void updateStorageLocationApi_shouldReturn200_andUpdatedBody() throws Exception {
        StorageLocationDto request = buildDto(1L, 1L, "B", "05", "03", true);
        StorageLocationDto response = buildDto(1L, 1L, "B", "05", "03", true);

        when(storageLocationService.update(any(StorageLocationDto.class))).thenReturn(response);

        mockMvc.perform(patch("/storageLocation/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.zone").value("B"))
                .andExpect(jsonPath("$.rack").value("05"))
                .andExpect(jsonPath("$.used").value(true));
    }

    @Test
    void updateStorageLocationApi_shouldReturn404_whenNotFound() throws Exception {
        StorageLocationDto request = buildDto(999L, 1L, "X", "99", "99", false);

        when(storageLocationService.update(any(StorageLocationDto.class)))
                .thenThrow(new NotFoundException("Ячейка с id 999 не найдена"));

        mockMvc.perform(patch("/storageLocation/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStorageLocationsApi_shouldReturn200_andPage() throws Exception {
        StorageLocationDto dto1 = buildDto(1L, 1L, "A", "01", "01", false);
        StorageLocationDto dto2 = buildDto(2L, 1L, "A", "01", "02", true);

        Page<StorageLocationDto> page = new PageImpl<>(List.of(dto1, dto2));

        when(storageLocationService.getAll(any(Pageable.class), eq(null))).thenReturn(page);

        mockMvc.perform(get("/storageLocation/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].zone").value("A"))
                .andExpect(jsonPath("$.content[1].used").value(true))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllStorageLocationsApi_shouldReturn200_withSearch() throws Exception {
        StorageLocationDto dto1 = buildDto(1L, 1L, "A", "01", "01", false);

        Page<StorageLocationDto> page = new PageImpl<>(List.of(dto1));

        when(storageLocationService.getAll(any(Pageable.class), eq("zone:A,"))).thenReturn(page);

        mockMvc.perform(get("/storageLocation/api").param("search", "zone:A,"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].zone").value("A"));
    }

    @Test
    void deleteStorageLocationApi_shouldReturn204_whenExists() throws Exception {
        long id = 1L;

        doNothing().when(storageLocationService).delete(id);

        mockMvc.perform(delete("/storageLocation/api/" + id))
                .andExpect(status().isNoContent());

        verify(storageLocationService).delete(eq(id));
    }

    @Test
    void deleteStorageLocationApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        doThrow(new NotFoundException("Ячейка с id 999 не найдена"))
                .when(storageLocationService).delete(id);

        mockMvc.perform(delete("/storageLocation/api/" + id))
                .andExpect(status().isNotFound());

        verify(storageLocationService).delete(eq(id));
    }
}

