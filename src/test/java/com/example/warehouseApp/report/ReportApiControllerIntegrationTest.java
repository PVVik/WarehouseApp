package com.example.warehouseApp.report;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.StorageLocationDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.movement.MovementDtoIssue;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
@ActiveProfiles("test")
public class ReportApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void turnoverJson_validParams_returnsListWithAbc_withIssues() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("Turnover Warehouse Issues");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "T", "4", "4", "T-4-4", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик Turn»", "5554443332", "555444333");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-TURN-001", "Товарооборот");

        MovementDtoReceipt receipt = createReceiptForMovement(nomen, warehouse, loc, supplier);
        Long receiptId = receipt.getId();

        LocalDate periodStart = LocalDate.of(2026, 10, 1);
        jdbcTemplate.update("""
                UPDATE movement
                SET document_date = ?
                WHERE id = ?
                """, periodStart, receiptId);

        var issueDto = new MovementDtoIssue();
        issueDto.setMovementType(MovementType.ISSUE);
        issueDto.setNomenclatureId(nomen.getId());
        issueDto.setWarehouseFrom(warehouse.getId());
        issueDto.setStorageLocationId(loc.getId());
        issueDto.setCustomerId(supplier.getId());
        issueDto.setQuantity(BigDecimal.ONE);
        issueDto.setPrice(BigDecimal.valueOf(500));

        String issueJson = objectMapper.writeValueAsString(issueDto);
        MvcResult issueResult = mockMvc.perform(post("/api/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MovementDtoIssue createdIssue = objectMapper.readValue(issueResult.getResponse().getContentAsString(), MovementDtoIssue.class);
        Long issueId = createdIssue.getId();

        LocalDate issueDate = LocalDate.of(2026, 10, 15);
        jdbcTemplate.update("""
                UPDATE movement
                SET document_date = ?
                WHERE id = ?
                """, issueDate, issueId);

        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        mockMvc.perform(get("/api/reports/turnover")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].nomenclatureName").value("Товарооборот"))
                .andExpect(jsonPath("$.[0].totalQuantity").value(1.0))
                .andExpect(jsonPath("$.[0].totalAmount").value(500.0))
                .andExpect(jsonPath("$.[0].group").isString());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deficitJson_validWarehouse_returnsDeficits() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("Deficit Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "D", "6", "6", "D-6-6", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик Def»", "7776665554", "777666555");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-DEF-001", "Дефицит товар");

        createReceiptForMovement(nomen, warehouse, loc, supplier);

        jdbcTemplate.update("""
                UPDATE nomenclature
                SET min_stock = ?
                WHERE id = ?
                """, BigDecimal.valueOf(100), nomen.getId());

        mockMvc.perform(get("/api/reports/deficit")
                        .param("warehouseId", String.valueOf(warehouse.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].nomenclatureName").value("Дефицит товар"))
                .andExpect(jsonPath("$.[0].minThreshold").value(100.0))
                .andExpect(jsonPath("$.[0].currentStock").value(1.0))
                .andExpect(jsonPath("$.[0].level").value("WARNING"))
                .andExpect(jsonPath("$.[0].deficitAmount").value(99.0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void nonLiquidJson_returnsNonLiquidItems() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("NonLiquid Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "N", "9", "9", "N-9-9", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Неликвид»", "1112223334", "111222333");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-NON-001", "Неликвидный товар");

        MovementDtoReceipt receipt = createReceiptForMovement(nomen, warehouse, loc, supplier);
        Long movementId = receipt.getId();

        LocalDate oldDate = LocalDate.now().minusDays(120);
        jdbcTemplate.update("""
                UPDATE movement
                SET document_date = ?
                WHERE id = ?
                """, oldDate, movementId);

        mockMvc.perform(get("/api/reports/nonliquid")
                        .param("warehouseId", String.valueOf(warehouse.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].nomenclatureName").value("Неликвидный товар"))
                .andExpect(jsonPath("$.[0].daysWithoutMovement").value(120))
                .andExpect(jsonPath("$.[0].critical").value(true))
                .andExpect(jsonPath("$.[0].status").value("КРИТИЧЕСКИЙ НЕЛИКВИД"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void nonLiquidCsv_returnsCsv() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("NonLiquid Csv Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "N", "10", "10", "N-10-10", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «CSV Неликвид»", "2223334445", "222333444");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-NON-CSV-001", "CSV неликвид");

        MovementDtoReceipt receipt = createReceiptForMovement(nomen, warehouse, loc, supplier);
        Long movementId = receipt.getId();

        LocalDate oldDate = LocalDate.now().minusDays(100);
        jdbcTemplate.update("""
                UPDATE movement
                SET document_date = ?
                WHERE id = ?
                """, oldDate, movementId);

        MvcResult result = mockMvc.perform(get("/api/reports/nonliquid/csv")
                        .param("warehouseId", String.valueOf(warehouse.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andReturn();

        assertThat(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .contains("nonliquid_warehouse_" + warehouse.getId());
        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deficitJson_noDeficits_returnsEmptyList() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("No Deficit Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "D", "7", "7", "D-7-7", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Нет дефицита»", "8887776665", "888777666");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-NO-DEF-001", "Товар без дефицита");

        createReceiptForMovement(nomen, warehouse, loc, supplier);

        jdbcTemplate.update("""
                UPDATE nomenclature
                SET min_stock = 0
                WHERE id = ?
                """, nomen.getId());

        mockMvc.perform(get("/api/reports/deficit")
                        .param("warehouseId", String.valueOf(warehouse.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void stockBalanceJson_validWarehouseId_returnsList() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("Test Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "Z", "1", "1", "Z-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-STOCK-001", "Товар для остатков");

        createReceiptForMovement(nomen, warehouse, loc, supplier);

        mockMvc.perform(get("/api/reports/stock-balance")
                        .param("warehouseId", String.valueOf(warehouse.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].quantity").isNumber())
                .andExpect(jsonPath("$.[0].sku").value("SKU-STOCK-001"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void stockBalanceJson_invalidWarehouseId_throwsBadRequest() throws Exception {
        prepareUser("admin", "ADMIN");
        mockMvc.perform(get("/api/reports/stock-balance")
                        .param("warehouseId", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void movementJson_validParams_returnsList() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("Movement Warehouse");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "M", "2", "2", "M-2-2", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик Mov»", "1234567890", "123456789");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-MOV-001", "Движение товара");

        createReceiptForMovement(nomen, warehouse, loc, supplier);

        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        mockMvc.perform(get("/api/reports/movement")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void movementJson_startDateAfterEndDate_throwsBadRequest() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now();

        mockMvc.perform(get("/api/reports/movement")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isBadRequest());
    }

    private MovementDtoReceipt createReceiptForMovement(
            NomenclatureDto nomenclature,
            WarehouseDto warehouse,
            StorageLocationDto location,
            CounterpartyDto counterparty) throws Exception {
        var receiptDto = new MovementDtoReceipt();
        receiptDto.setMovementType(MovementType.RECEIPT);
        receiptDto.setNomenclatureId(nomenclature.getId());
        receiptDto.setWarehouseTo(warehouse.getId());
        receiptDto.setStorageLocationId(location.getId());
        receiptDto.setCounterpartyId(counterparty.getId());
        receiptDto.setQuantity(BigDecimal.ONE);
        receiptDto.setPrice(BigDecimal.valueOf(1000));

        String receiptJson = objectMapper.writeValueAsString(receiptDto);
        MvcResult result = mockMvc.perform(post("/api/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiptJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MovementDtoReceipt.class);
    }

    private WarehouseDto createWarehouseAndReturnDto(String name) throws Exception {
        var dto = new WarehouseDto();
        dto.setName(name);
        dto.setActive(true);
        dto.setAddress("Адрес");
        dto.setType(WarehouseType.REMOTE);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), WarehouseDto.class);
    }

    private StorageLocationDto createStorageLocationAndReturnDto(Long warehouseId, String zone, String rack, String shelf, String code, boolean used) throws Exception {
        var dto = new StorageLocationDto();
        dto.setWarehouseId(warehouseId);
        dto.setZone(zone);
        dto.setRack(rack);
        dto.setShelf(shelf);
        dto.setUsed(used);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), StorageLocationDto.class);
    }

    private CounterpartyDto createCounterpartyAndReturnDto(String name, String inn, String kpp) throws Exception {
        var dto = new CounterpartyDto();
        dto.setName(name);
        dto.setInn(inn);
        dto.setKpp(kpp);
        dto.setCounterpartyType(CounterpartyType.SUPPLIER);
        dto.setIsActive(true);
        dto.setContact("contact");
        dto.setPhone("89999999999");

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CounterpartyDto.class);
    }

    private NomenclatureDto createNomenclatureAndReturnDto(String sku, String name) throws Exception {
        var dto = new NomenclatureDto();
        dto.setName(name);
        dto.setSku(sku);
        dto.setStuffCategory(StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(InventoryType.QUANTITY);
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
}

