package com.example.warehouseApp.documents;

import com.example.warehouseApp.controller.documents.EndActApiController;
import com.example.warehouseApp.dto.documents.ReceiptDocumentResultDto;
import com.example.warehouseApp.dto.documents.WriteOffDocumentResultDto;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.model.DocumentStatus;
import com.example.warehouseApp.service.documents.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EndActApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EndActApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    private final Long actId = 123L;
    private final String reason = "Проверка расхождений";
    private final String source = "Инвентаризация";

    @Test
    void completeWriteOffByAct_shouldReturn200_withAdmin() throws Exception {
        var resultDto = new WriteOffDocumentResultDto(
                actId, DocumentStatus.COMPLETED, reason, LocalDate.now(), 99L, List.of()
        );

        when(documentService.completeWriteOffByActId(actId, reason))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/end-act/write-off/{actNumber}/complete", actId)
                        .param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(actId))
                .andExpect(jsonPath("$.status").value(DocumentStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.reason").value(reason));

        verify(documentService).completeWriteOffByActId(actId, reason);
    }

    @Test
    void completeWriteOffByAct_shouldReturn200_withStorekeeper() throws Exception {
        var resultDto = new WriteOffDocumentResultDto(
                actId, DocumentStatus.COMPLETED, reason, LocalDate.now(), 99L, List.of()
        );

        when(documentService.completeWriteOffByActId(actId, reason))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/end-act/write-off/{actNumber}/complete", actId)
                        .param("reason", reason))
                .andExpect(status().isOk());

        verify(documentService).completeWriteOffByActId(actId, reason);
    }

    @Test
    void completeReceiptByAct_shouldReturn200() throws Exception {
        var resultDto = new ReceiptDocumentResultDto(
                actId, DocumentStatus.COMPLETED, source, LocalDate.now(), 99L, List.of()
        );

        when(documentService.completeReceiptByActId(actId, source))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/end-act/receipt/{actId}/complete", actId)
                        .param("source", source))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value(source));

        verify(documentService).completeReceiptByActId(actId, source);
    }

    @Test
    void completeWriteOffByAct_shouldHandleNotFound() throws Exception {
        when(documentService.completeWriteOffByActId(anyLong(), anyString()))
                .thenThrow(new NotFoundException("Акт не найден"));

        mockMvc.perform(post("/api/end-act/write-off/{actNumber}/complete", actId)
                        .param("reason", reason))
                .andExpect(status().isNotFound());
    }

    @Test
    void completeWriteOffByAct_shouldHandleIllegalState() throws Exception {
        when(documentService.completeWriteOffByActId(anyLong(), anyString()))
                .thenThrow(new IllegalStateException("Документ не в статусе DRAFT"));

        mockMvc.perform(post("/api/end-act/write-off/{actNumber}/complete", actId)
                        .param("reason", reason))
                .andExpect(status().isInternalServerError());
    }
}

