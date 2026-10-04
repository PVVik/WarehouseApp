package com.example.WarehouseApp.warehouse;

import com.example.WarehouseApp.controller.warehouse.WarehouseController;
import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.model.WarehouseType;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaseService<WarehouseDto> warehouseService;

    @Test
    void addWarehouseApi_shouldReturn201_andJsonBody() throws Exception {
        WarehouseDto request = new WarehouseDto();
        request.setName("Склад 1");
        request.setAddress("г. Москва, ул. Ленина, 1");
        request.setType(WarehouseType.CENTRAL);
        request.setActive(true);

        WarehouseDto response = new WarehouseDto();
        response.setId(1L);
        response.setName("Склад 1");
        response.setAddress("г. Москва, ул. Ленина, 1");
        response.setType(WarehouseType.CENTRAL);
        response.setActive(true);

        when(warehouseService.create(any(WarehouseDto.class))).thenReturn(response);

        mockMvc.perform(post("/warehouse/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Склад 1"))
                .andExpect(jsonPath("$.type").value("CENTRAL"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void addWarehouseApi_shouldReturn400_whenNameIsBlank() throws Exception {
        WarehouseDto request = new WarehouseDto();
        request.setName("");
        request.setAddress("г. Москва");
        request.setType(WarehouseType.REMOTE);
        request.setActive(true);

        mockMvc.perform(post("/warehouse/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(warehouseService, never()).create(any());
    }

    @Test
    void addWarehouseApi_shouldReturn400_whenAddressIsBlank() throws Exception {
        WarehouseDto request = new WarehouseDto();
        request.setName("Склад");
        request.setAddress("");
        request.setType(WarehouseType.REMOTE);
        request.setActive(true);

        mockMvc.perform(post("/warehouse/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(warehouseService, never()).create(any());
    }


    @Test
    void getWarehousesApi_shouldReturn200_andList() throws Exception {
        WarehouseDto dto1 = new WarehouseDto();
        dto1.setId(1L);
        dto1.setName("Склад A");
        dto1.setType(WarehouseType.CENTRAL);
        dto1.setActive(true);

        WarehouseDto dto2 = new WarehouseDto();
        dto2.setId(2L);
        dto2.setName("Склад B");
        dto2.setType(WarehouseType.REMOTE);
        dto2.setActive(false);

        Page<WarehouseDto> page = new PageImpl<>(List.of(dto1, dto2));

        when(warehouseService.getAll(any(Pageable.class), eq(null))).thenReturn(page);

        mockMvc.perform(get("/warehouse/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Склад A"))
                .andExpect(jsonPath("$.content[1].name").value("Склад B"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getWarehouseByIdApi_shouldReturn200_whenExists() throws Exception {
        WarehouseDto dto = new WarehouseDto();
        dto.setId(1L);
        dto.setName("Склад 1");
        dto.setAddress("Адрес 1");
        dto.setType(WarehouseType.CENTRAL);
        dto.setActive(true);

        when(warehouseService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/warehouse/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Склад 1"))
                .andExpect(jsonPath("$.type").value("CENTRAL"));
    }

    @Test
    void getWarehouseByIdApi_shouldReturn404_whenNotExists() throws Exception {
        Long id = 999L;

        when(warehouseService.getById(anyLong()))
                .thenThrow(new NotFoundException(String.format("Склад с id %d не найден", id)));

        mockMvc.perform(get("/warehouse/api/" + id))
                .andExpect(status().isNotFound());

        verify(warehouseService).getById(eq(id));
    }

    @Test
    void updateWarehouseApi_shouldReturn200_andUpdatedBody() throws Exception {
        WarehouseDto request = new WarehouseDto();
        request.setId(1L);
        request.setName("Обновлённый склад");
        request.setAddress("Новый адрес");
        request.setType(WarehouseType.REMOTE);
        request.setActive(false);

        WarehouseDto response = new WarehouseDto();
        response.setId(1L);
        response.setName("Обновлённый склад");
        response.setAddress("Новый адрес");
        response.setType(WarehouseType.REMOTE);
        response.setActive(false);

        when(warehouseService.update(any(WarehouseDto.class))).thenReturn(response);

        mockMvc.perform(patch("/warehouse/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Обновлённый склад"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateWarehouseApi_shouldReturn404_whenWarehouseNotFound() throws Exception {
        WarehouseDto request = new WarehouseDto();
        request.setId(999L);
        request.setName("Несуществующий склад");
        request.setAddress("Нигде");
        request.setType(WarehouseType.CENTRAL);
        request.setActive(true);

        when(warehouseService.update(any(WarehouseDto.class)))
                .thenThrow(new NotFoundException("Склад с id 999 не найден"));

        mockMvc.perform(patch("/warehouse/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWarehouseApi_shouldReturn204_whenExists() throws Exception {
        long id = 1L;

        doNothing().when(warehouseService).delete(id);

        mockMvc.perform(delete("/warehouse/api/" + id))
                .andExpect(status().isNoContent());

        verify(warehouseService).delete(eq(id));
    }

    @Test
    void deleteWarehouseApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        doThrow(new NotFoundException("Склад с id 999 не найден"))
                .when(warehouseService).delete(id);

        mockMvc.perform(delete("/warehouse/api/" + id))
                .andExpect(status().isNotFound());

        verify(warehouseService).delete(eq(id));
    }
}
