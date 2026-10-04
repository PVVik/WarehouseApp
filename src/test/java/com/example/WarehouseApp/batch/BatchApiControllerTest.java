package com.example.WarehouseApp.batch;

import com.example.WarehouseApp.controller.batch.BatchApiController;
import com.example.WarehouseApp.dto.BatchDto;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BatchApiController.class)
class BatchApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaseService<BatchDto> batchService;

    @Test
    void addBatchApi_shouldReturn201_andJsonBody() throws Exception {
        var request = buildDto(null, 1L, "BATCH-2024-001", 5L, "CERT-12345");
        var response = buildDto(1L, 1L, "BATCH-2024-001", 5L, "CERT-12345");

        when(batchService.create(any(BatchDto.class))).thenReturn(response);

        mockMvc.perform(post("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomenclatureId").value(1))
                .andExpect(jsonPath("$.batchNumber").value("BATCH-2024-001"))
                .andExpect(jsonPath("$.supplierId").value(5))
                .andExpect(jsonPath("$.certificateNumber").value("CERT-12345"))
                .andExpect(jsonPath("$.productionDate").value("2024-06-01"))
                .andExpect(jsonPath("$.expiryDate").value("2025-06-01"));

        verify(batchService).create(any(BatchDto.class));
    }

    @Test
    void addBatchApi_shouldReturn400_whenNomenclatureIdIsNull() throws Exception {
        var json = "{\"nomenclatureId\": null, \"batchNumber\": \"BATCH-BAD-001\"}";

        mockMvc.perform(post("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(batchService, never()).create(any());
    }

    @Test
    void addBatchApi_shouldReturn400_whenBatchNumberIsNull() throws Exception {
        var json = "{\"nomenclatureId\": 1, \"batchNumber\": null}";

        mockMvc.perform(post("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(batchService, never()).create(any());
    }

    @Test
    void addBatchApi_shouldReturn400_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(batchService, never()).create(any());
    }

    @Test
    void getBatchByIdApi_shouldReturn200_whenExists() throws Exception {
        var dto = buildDto(1L, 1L, "BATCH-2024-001", 5L, "CERT-12345");

        when(batchService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/batch/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchNumber").value("BATCH-2024-001"))
                .andExpect(jsonPath("$.supplierId").value(5))
                .andExpect(jsonPath("$.receiptDate").value("2024-06-02"));

        verify(batchService).getById(eq(1L));
    }

    @Test
    void getBatchByIdApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        when(batchService.getById(id))
                .thenThrow(new NotFoundException(String.format("Партия с id %d не найдена", id)));

        mockMvc.perform(get("/batch/api/" + id))
                .andExpect(status().isNotFound());

        verify(batchService).getById(eq(id));
    }

    @Test
    void updateBatchApi_shouldReturn200_andUpdatedBody() throws Exception {
        var request = buildDto(1L, 1L, "BATCH-2024-001", 5L, "CERT-UPDATED-999");
        var response = buildDto(1L, 1L, "BATCH-2024-001", 5L, "CERT-UPDATED-999");

        when(batchService.update(any(BatchDto.class))).thenReturn(response);

        mockMvc.perform(patch("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.certificateNumber").value("CERT-UPDATED-999"));

        verify(batchService).update(any(BatchDto.class));
    }

    @Test
    void updateBatchApi_shouldReturn404_whenNotFound() throws Exception {
        var request = buildDto(999L, 1L, "BATCH-FAKE", null, null);

        when(batchService.update(any(BatchDto.class)))
                .thenThrow(new NotFoundException("Партия с id 999 не найдена"));

        mockMvc.perform(patch("/batch/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBatchesApi_shouldReturn200_andPage() throws Exception {
        var dto1 = buildDto(1L, 1L, "BATCH-001", 5L, "CERT-1");
        var dto2 = buildDto(2L, 1L, "BATCH-002", null, null);

        var page = new PageImpl<>(List.of(dto1, dto2));

        when(batchService.getAll(any(Pageable.class), eq(null))).thenReturn(page);

        mockMvc.perform(get("/batch/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].batchNumber").value("BATCH-001"))
                .andExpect(jsonPath("$.content[1].supplierId").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllBatchesApi_shouldReturn200_withSearch() throws Exception {
        var dto1 = buildDto(1L, 1L, "BATCH-001", 5L, "CERT-1");

        var page = new PageImpl<>(List.of(dto1));

        when(batchService.getAll(any(Pageable.class), eq("batchNumber:BATCH,"))).thenReturn(page);

        mockMvc.perform(get("/batch/api").param("search", "batchNumber:BATCH,"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].batchNumber").value("BATCH-001"));
    }

    @Test
    void deleteBatchApi_shouldReturn204_whenExists() throws Exception {
        long id = 1L;

        doNothing().when(batchService).delete(id);

        mockMvc.perform(delete("/batch/api/" + id))
                .andExpect(status().isNoContent());

        verify(batchService).delete(eq(id));
    }

    @Test
    void deleteBatchApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        doThrow(new NotFoundException("Партия с id 999 не найдена"))
                .when(batchService).delete(id);

        mockMvc.perform(delete("/batch/api/" + id))
                .andExpect(status().isNotFound());

        verify(batchService).delete(eq(id));
    }

    private BatchDto buildDto(Long id, Long nomenclatureId, String batchNumber, Long supplierId,
                              String certificateNumber) {
        var dto = new BatchDto();
        if (id != null) {
            dto.setId(id);
        }
        dto.setNomenclatureId(nomenclatureId);
        dto.setBatchNumber(batchNumber);
        dto.setSupplierId(supplierId);
        dto.setProductionDate(LocalDate.of(2024, 6, 1));
        dto.setExpiryDate(LocalDate.of(2025, 6, 1));
        dto.setCertificateNumber(certificateNumber);
        dto.setReceiptDate(LocalDate.of(2024, 6, 2));
        return dto;
    }
}
