package com.example.warehouseApp.stockBalance;

import com.example.warehouseApp.controller.stockBalance.StockBalancesController;
import com.example.warehouseApp.dto.StockBalanceResponseDto;
import com.example.warehouseApp.parser.stockBalance.StockBalanceCsvParser;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebMvcTest(StockBalancesController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockBalancesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockBalanceCsvParser stockBalanceCsvParser;

    @MockitoBean
    private StockBalanceService stockBalanceService;

    @Test
    void getStockBalances_withoutWarehouseId_shouldReturn200() throws Exception {
        var dto1 = new StockBalanceResponseDto(
                "SKU-001",
                "Склад Центральный",
                "Номенклатура А",
                BigDecimal.valueOf(150.5),
                "шт"
        );
        var dto2 = new StockBalanceResponseDto(
                "SKU-002",
                "Склад Филиал",
                "Номенклатура Б",
                BigDecimal.valueOf(30.0),
                "кг"
        );

        List<StockBalanceResponseDto> responseList = List.of(dto1, dto2);

        when(stockBalanceService.getBalances(null)).thenReturn(responseList);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/stock-balances")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantity").value(150.5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uom").value("шт"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sku").value("SKU-002"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].quantity").value(30.0));
    }

    @Test
    void getStockBalances_withWarehouseId_shouldReturn200() throws Exception {
        Long warehouseId = 5L;

        var dto = new StockBalanceResponseDto(
                "SKU-999",
                "Склад Спец",
                "Спецноменклатура",
                BigDecimal.valueOf(10.25),
                "л"
        );

        List<StockBalanceResponseDto> responseList = List.of(dto);

        when(stockBalanceService.getBalances(warehouseId)).thenReturn(responseList);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/stock-balances")
                                .param("warehouseId", String.valueOf(warehouseId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sku").value("SKU-999"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantity").value(10.25))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uom").value("л"));
    }

    @Test
    void getStockBalances_shouldReturnEmptyList() throws Exception {
        when(stockBalanceService.getBalances(anyLong())).thenReturn(List.of());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/stock-balances")
                                .param("warehouseId", "7")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(0));
    }
}
