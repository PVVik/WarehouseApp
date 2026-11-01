package com.example.warehouseApp.sync;

import com.example.warehouseApp.controller.sync.SyncController;
import com.example.warehouseApp.dto.sync.SyncOperationDto;
import com.example.warehouseApp.dto.sync.SyncRequest;
import com.example.warehouseApp.dto.sync.SyncResponse;
import com.example.warehouseApp.dto.sync.SyncResultItem;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.service.sync.SyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(SyncController.class)
@AutoConfigureMockMvc(addFilters = false)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private SyncService syncService;

    @Test
    void sync_shouldReturn200_andResponse() throws Exception {
        var op1 = new SyncOperationDto();
        op1.setType(MovementType.RECEIPT);
        op1.setNomenclatureSku("SKU-001");
        op1.setWarehouseName("Основной склад");
        op1.setQuantity(BigDecimal.valueOf(10.5));
        op1.setDate(LocalDate.of(2026, 10, 31));
        op1.setDocumentNumber("DOC-123");
        op1.setBatchNumber("BATCH-001");
        op1.setBatchProductionDate(LocalDate.of(2026, 9, 1));
        op1.setBatchExpiryDate(LocalDate.of(2027, 9, 1));
        op1.setSerialNumber("SN-1111");
        op1.setPassportNumber("PSP-111");

        var op2 = new SyncOperationDto();
        op2.setType(MovementType.ISSUE);
        op2.setNomenclatureSku("SKU-002");
        op2.setWarehouseName("Филиал МСК");
        op2.setQuantity(BigDecimal.valueOf(5));
        op2.setDate(LocalDate.now());
        op2.setDocumentNumber("DOC-456");

        var request = new SyncRequest();
        request.setSiteCode("MSK-01");
        request.setSyncToken("token-abc-123");
        request.setOperations(List.of(op1, op2));

        var result1 = new SyncResultItem();
        result1.setIndex(0);
        result1.setSuccess(true);
        result1.setMessage("Успешно создано");
        result1.setCreatedId(100L);

        var result2 = new SyncResultItem();
        result2.setIndex(1);
        result2.setSuccess(false);
        result2.setMessage("Не найдена номенклатура SKU-002");
        result2.setCreatedId(null);

        var response = new SyncResponse();
        response.setSiteCode("MSK-01");
        response.setTotalCount(2);
        response.setSuccessCount(1);
        response.setFailureCount(1);
        response.setResults(List.of(result1, result2));

        when(syncService.sync(any(SyncRequest.class))).thenReturn(response);

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/sync")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.siteCode").value("MSK-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.successCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].index").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].message").value("Успешно создано"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].createdId").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[1].index").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[1].success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[1].message").value("Не найдена номенклатура SKU-002"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[1].createdId").doesNotExist());
    }

}
