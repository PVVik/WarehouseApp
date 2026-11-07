package com.example.warehouseApp.sync;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.sync.SyncOperationDto;
import com.example.warehouseApp.dto.sync.SyncRequest;
import com.example.warehouseApp.model.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_db",
        "spring.datasource.username=warehouse_user",
        "spring.datasource.password=warehouse_pass",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SyncControllerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

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
    void shouldSyncMultipleOperationsSuccessfully() throws Exception {
        prepareReferenceData();

        List<SyncOperationDto> operations = Arrays.asList(
                new SyncOperationDto(
                        MovementType.RECEIPT,
                        "SKU-001",
                        "Central Warehouse",
                        BigDecimal.valueOf(10),
                        LocalDate.now(),
                        "DOC-001",
                        "BATCH-001",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(3000, 1, 1),
                        null,
                        null
                ),
                new SyncOperationDto(
                        MovementType.ISSUE,
                        "SKU-002",
                        "Remote Warehouse",
                        BigDecimal.valueOf(-5),
                        LocalDate.now().minusDays(1),
                        "DOC-002",
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        SyncRequest request = new SyncRequest("SITE-001", "TOKEN-ABC", operations);

        mockMvc.perform(post("/api/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteCode").value("SITE-001"))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.results[0].success").value(true))
                .andExpect(jsonPath("$.results[1].success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenWarehouseNameIsMissing() throws Exception {
        prepareReferenceData();

        List<SyncOperationDto> operations = Arrays.asList(
                new SyncOperationDto(
                        MovementType.RECEIPT,
                        "SKU-001",
                        "",
                        BigDecimal.TEN,
                        LocalDate.now(),
                        "DOC-FAIL-1",
                        null, null, null, null, null
                )
        );

        SyncRequest request = new SyncRequest("SITE-001", "TOKEN-ABC", operations);

        mockMvc.perform(post("/api/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.results[0].success").value(false))
                .andExpect(jsonPath("$.results[0].message").value("Не указано имя склада"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenSerialUsedOnNonSerialNomenclature() throws Exception {
        prepareReferenceData();

        List<SyncOperationDto> operations = Arrays.asList(
                new SyncOperationDto(
                        MovementType.RECEIPT,
                        "SKU-001",
                        "Central Warehouse",
                        BigDecimal.ONE,
                        LocalDate.now(),
                        "DOC-FAIL-2",
                        null, null, null,
                        "BAD-SN-123",
                        null
                )
        );

        SyncRequest request = new SyncRequest("SITE-001", "TOKEN-ABC", operations);

        mockMvc.perform(post("/api/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.results[0].success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleEmptyOperationsList() throws Exception {
        prepareReferenceData();

        SyncRequest request = new SyncRequest("SITE-001", "TOKEN-ABC", List.of());

        mockMvc.perform(post("/api/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(0));
    }

    private void prepareReferenceData() throws Exception {
        createWarehouse("Central Warehouse", "Addr Central", true);
        createWarehouse("Remote Warehouse", "Addr Remote", true);
        createNomenclature("Nom 1", "SKU-001");
        createNomenclature("Nom 2", "SKU-002");
        createNomenclature("Nom 3", "SKU-SERIAL-001");
        jdbcTemplate.update("""
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 1, "user", "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR", "test@example.com", "Test User", "ADMIN", true);
    }

    private void createWarehouse(String name, String address, boolean active) throws Exception {
        var dto = new WarehouseDto();
        dto.setName(name);
        dto.setAddress(address);
        dto.setType(WarehouseType.CENTRAL);
        dto.setActive(active);

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    private void createNomenclature(String name, String sku) throws Exception {
        var dto = new NomenclatureDto();
        dto.setName(name);
        dto.setSku(sku);
        dto.setStuffCategory(StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(InventoryType.QUANTITY);
        dto.setActive(true);

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

}
