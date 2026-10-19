package com.example.warehouseApp.analytics;

import com.example.warehouseApp.controller.analytics.expiryControl.ExpiryControlApiController;
import com.example.warehouseApp.dto.analytics.ExpiryControlItemDto;
import com.example.warehouseApp.service.analytics.ExpiryControlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpiryControlApiController.class)
class ExpiryControlApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpiryControlService expiryControlService;

    @Test
    void shouldHandlePagination() throws Exception {
        List<ExpiryControlItemDto> items = List.of(
                new ExpiryControlItemDto(3L, "BATCH-003", "Номенклатура В", LocalDate.now(), "EXPIRED")
        );
        Page<ExpiryControlItemDto> page = new PageImpl<>(items, PageRequest.of(1, 5), 10);

        when(expiryControlService.getControlList(any(Pageable.class), eq(null)))
                .thenReturn(page);

        mockMvc.perform(get("/api/analytics/expiry-control?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageNumber", is(1)))
                .andExpect(jsonPath("$.pageable.pageSize", is(5)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldPassSearchParameter() throws Exception {
        String searchQuery = "ABC";
        List<ExpiryControlItemDto> items = List.of();
        Page<ExpiryControlItemDto> page = new PageImpl<>(items);

        when(expiryControlService.getControlList(any(Pageable.class), eq(searchQuery)))
                .thenReturn(page);

        mockMvc.perform(get("/api/analytics/expiry-control").param("search", searchQuery))
                .andExpect(status().isOk());

        verify(expiryControlService).getControlList(any(Pageable.class), eq(searchQuery));
    }
}