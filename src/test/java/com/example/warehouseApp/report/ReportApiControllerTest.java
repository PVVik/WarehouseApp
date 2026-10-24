package com.example.warehouseApp.report;
// <-- ЗАМЕНИ НА СВОЙ ПАКЕТ КОНТРОЛЛЕРА

import com.example.warehouseApp.controller.report.ReportApiController;
import com.example.warehouseApp.dto.reports.*;
import com.example.warehouseApp.model.MovementType;
import com.example.warehouseApp.model.UnitOfMeasure;
import com.example.warehouseApp.service.reports.ReportService;
import com.example.warehouseApp.util.CsvGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(ReportApiController.class)
class ReportApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private CsvGenerator csvGenerator;

    private static final Long WAREHOUSE_ID = 123L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 01);
    private static final LocalDate END_DATE = LocalDate.of(2026, 10, 31);
    private static final MovementType MOCK_MOVEMENT_TYPE = MovementType.RECEIPT;
    private static final UnitOfMeasure MOCK_UOM = UnitOfMeasure.LITER;
    private static final DeficitReportDto.DeficitLevel MOCK_DEFICIT_LEVEL = DeficitReportDto.DeficitLevel.CRITICAL;

    @Test
    void shouldReturnStockBalanceJson() throws Exception {
        var data = List.of(
                new StockBalanceReportDto(
                        "SKU-001",
                        "Товар А",
                        "BATCH-123",
                        "Основной склад",
                        "A-01-05",
                        java.math.BigDecimal.valueOf(100),
                        MOCK_UOM
                )
        );

        when(reportService.getStockBalanceReport(WAREHOUSE_ID)).thenReturn(data);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/stock-balance")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sku", org.hamcrest.Matchers.is("SKU-001")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantity", org.hamcrest.Matchers.is(100)));
    }

    @Test
    void shouldRejectInvalidWarehouseIdForStockBalanceJson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/stock-balance")
                        .param("warehouseId", "0"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void shouldReturnCsvForStockBalance() throws Exception {
        byte[] csvBytes = "sku,name,quantity\nSKU-001,Товар А,100".getBytes(StandardCharsets.UTF_8);

        var data = List.of(
                new StockBalanceReportDto("SKU-001", "Товар А", "BATCH-123", "Основной склад", "A-01-05", java.math.BigDecimal.valueOf(100), MOCK_UOM)
        );

        when(reportService.getStockBalanceReport(WAREHOUSE_ID)).thenReturn(data);
        when(csvGenerator.generateStockBalanceCsv(data)).thenReturn(csvBytes);

        String filename = "stock_balance_warehouse_123.csv";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/stock-balance/csv")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andReturn();

        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo(new String(csvBytes, StandardCharsets.UTF_8));
    }

    @Test
    void shouldReturnEmptyCsvWhenNoDataForStockBalance() throws Exception {
        byte[] emptyCsv = "sku,name,quantity".getBytes(StandardCharsets.UTF_8);

        when(reportService.getStockBalanceReport(WAREHOUSE_ID)).thenReturn(List.of());
        when(csvGenerator.generateEmptyStockBalanceCsv()).thenReturn(emptyCsv);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/stock-balance/csv")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void shouldReturnMovementJsonWithDates() throws Exception {
        Map<String, Object> item = new HashMap<>();
        item.put("date", LocalDate.now());
        item.put("documentType", MOCK_MOVEMENT_TYPE.name()); // или toString()
        item.put("documentNumber", "DOC-001");
        item.put("nomenclatureName", "Товар М");
        item.put("qty", BigDecimal.valueOf(50));

        List<Map<String, Object>> rawData = new ArrayList<>();
        rawData.add(item);

        List<MovementReportProjection> typedData = (List<MovementReportProjection>) (List<?>) rawData;

        when(reportService.getMovementReport(WAREHOUSE_ID, START_DATE, END_DATE))
                .thenReturn(typedData);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/movement")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                        .param("startDate", START_DATE.toString())
                        .param("endDate", END_DATE.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nomenclatureName", Matchers.is("Товар М")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].qty", Matchers.is(50)));
    }

    @Test
    void shouldRejectBadDatesForMovement() throws Exception {
        LocalDate badStart = LocalDate.of(2026, 12, 01);
        LocalDate badEnd = LocalDate.of(2026, 11, 01);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/movement")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                        .param("startDate", badStart.toString())
                        .param("endDate", badEnd.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void shouldReturnTurnoverJson() throws Exception {
        var data = List.of(
                new TurnoverReportDto(
                        1L,
                        "Товар Т",
                        java.math.BigDecimal.valueOf(1200),
                        java.math.BigDecimal.valueOf(100),
                        java.math.BigDecimal.valueOf(50000),
                        "A"
                )
        );

        when(reportService.getTurnoverReportWithAbc(WAREHOUSE_ID, START_DATE, END_DATE)).thenReturn(data);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/turnover")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                        .param("startDate", START_DATE.toString())
                        .param("endDate", END_DATE.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nomenclatureName", org.hamcrest.Matchers.is("Товар Т")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].group", org.hamcrest.Matchers.is("A")));
    }

    @Test
    void shouldReturnTurnoverCsv() throws Exception {
        byte[] csvBytes = "nomenclatureName,totalQuantity,group\nТовар Т,1200,A".getBytes(StandardCharsets.UTF_8);

        var data = List.of(
                new TurnoverReportDto(1L, "Товар Т", java.math.BigDecimal.valueOf(1200),
                        java.math.BigDecimal.valueOf(100), java.math.BigDecimal.valueOf(50000), "A")
        );

        String filename = "turnover_report_warehouse_123_2026-10-01_to_2026-10-31.csv";

        when(reportService.getTurnoverReportWithAbc(WAREHOUSE_ID, START_DATE, END_DATE)).thenReturn(data);
        when(csvGenerator.generateTurnoverReportCsv(data)).thenReturn(csvBytes);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/turnover/csv")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID))
                        .param("startDate", START_DATE.toString())
                        .param("endDate", END_DATE.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andReturn();

        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo(new String(csvBytes, StandardCharsets.UTF_8));
    }

    @Test
    void shouldReturnDeficitJson() throws Exception {
        var data = List.of(
                new DeficitReportDto(
                        "Товар Д",
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.valueOf(50),
                        MOCK_DEFICIT_LEVEL,
                        java.math.BigDecimal.valueOf(50)
                )
        );

        when(reportService.getDeficitReport(WAREHOUSE_ID)).thenReturn(data);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/deficit")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nomenclatureName", org.hamcrest.Matchers.is("Товар Д")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].level", org.hamcrest.Matchers.is("CRITICAL")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].deficitAmount", org.hamcrest.Matchers.is(50)));
    }

    @Test
    void shouldReturnNonLiquidJson() throws Exception {
        var data = List.of(
                new NonLiquidReportDto(
                        1L,
                        "Товар Н",
                        "SKU-N001",
                        "Основной склад",
                        "BATCH-999",
                        java.math.BigDecimal.valueOf(10),
                        java.math.BigDecimal.valueOf(20),
                        LocalDate.of(2026, 9, 1),
                        95,
                        true,
                        "НЕЛИКВИД"
                )
        );

        LocalDate testDate = LocalDate.now();

        when(reportService.getNonLiquidReport(WAREHOUSE_ID, testDate)).thenReturn(data);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/nonliquid")
                        .param("warehouseId", String.valueOf(WAREHOUSE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nomenclatureName", Matchers.is("Товар Н")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].daysWithoutMovement", Matchers.is(95)));
    }

}
