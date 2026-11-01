package com.example.warehouseApp.analytics;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.model.CounterpartyType;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.StuffCategory;
import com.example.warehouseApp.model.UnitOfMeasure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_db",
        "spring.datasource.username=warehouse_user",
        "spring.datasource.password=warehouse_pass",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
class ExpiryControlApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnEmptyPageWhenNoBatches() throws Exception {
        prepareUser("admin", "ADMIN");

        mockMvc.perform(get("/api/analytics/expiry-control")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPageWithBatchesAndNomenclatureNames() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-EXP-001", "Материал с партиями");
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "1234567890", "123456789");

        for (int i = 0; i < 25; i++) {
            var batchDto = createValidBatchDto(nomen.getId(), supplier.getId());
            batchDto.setBatchNumber("BATCH-" + i);
            batchDto.setExpiryDate(LocalDate.now().plusDays(i + 10));
            String json = objectMapper.writeValueAsString(batchDto);
            mockMvc.perform(post("/api/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/analytics/expiry-control?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].batchNumber").value("BATCH-0"))
                .andExpect(jsonPath("$.content[0].nomenclatureName").value("Материал с партиями"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldFilterBySearchPattern() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        NomenclatureDto nomen1 = createNomenclatureAndReturnDto("SKU-EXP-A", "Альфа-материал");
        NomenclatureDto nomen2 = createNomenclatureAndReturnDto("SKU-EXP-B", "Бета-материал");

        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "1234567890", "123456789");

        var b1 = createValidBatchDto(nomen1.getId(), supplier.getId());
        b1.setBatchNumber("BATCH-ALPHA-001");
        b1.setExpiryDate(LocalDate.now().plusDays(5));

        var b2 = createValidBatchDto(nomen2.getId(), supplier.getId());
        b2.setBatchNumber("BATCH-BETA-002");
        b2.setExpiryDate(LocalDate.now().plusDays(20));

        Stream.of(b1, b2).forEach(b -> {
            try {
                mockMvc.perform(post("/api/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(b)))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(get("/api/analytics/expiry-control?search=batchNumber:ALPHA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].batchNumber").value("BATCH-ALPHA-001"))
                .andExpect(jsonPath("$.content[0].nomenclatureName").value("Альфа-материал"));
    }

    private BatchDto createValidBatchDto(Long nomenclatureId, Long supplierId) {
        var dto = new BatchDto();
        dto.setNomenclatureId(nomenclatureId);
        dto.setBatchNumber("BATCH-001");
        dto.setSupplierId(supplierId);
        dto.setProductionDate(LocalDate.now().minusDays(10));
        dto.setExpiryDate(LocalDate.now().plusDays(30));
        dto.setReceiptDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        return dto;
    }

    private void prepareUser(String username, String role) throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
        jdbcTemplate.update("""
                DELETE FROM users WHERE username = ?
                """, username);
        jdbcTemplate.update("""
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 10, username, encodedPassword, username + "@test.com", "Test User", role, true);
    }

    private NomenclatureDto createNomenclatureAndReturnDto(String sku, String name) throws Exception {
        var dto = new NomenclatureDto();
        dto.setName(name);
        dto.setSku(sku);
        dto.setStuffCategory(StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(InventoryType.BATCH);
        dto.setActive(true);
        dto.setMinStock(BigDecimal.TEN);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), NomenclatureDto.class);
    }

    private CounterpartyDto createCounterpartyAndReturnDto(String name, String inn, String kpp) throws Exception {
        var dto = new CounterpartyDto();
        dto.setName(name);
        dto.setInn(inn);
        dto.setKpp(kpp);
        dto.setCounterpartyType(CounterpartyType.SUPPLIER);
        dto.setPhone("+79001112233");
        dto.setContact("Иванов И.И.");
        dto.setIsActive(true);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CounterpartyDto.class);
    }
}
