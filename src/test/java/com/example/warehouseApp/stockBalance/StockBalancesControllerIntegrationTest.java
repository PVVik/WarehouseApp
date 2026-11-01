package com.example.warehouseApp.stockBalance;

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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StockBalancesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_get_all_stock_balances() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №1");
        StorageLocationDto location = createStorageLocationAndReturnDto(warehouse.getId(), "A", "1", "1", "A-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BAL-001", "Товар для теста остатков", InventoryType.QUANTITY);

        createReceiptForWriteOff(nomenclature, warehouse, location, supplier, null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stock-balances")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-BAL-001"))
                .andExpect(jsonPath("$[0].warehouseName").value(warehouse.getName()))
                .andExpect(jsonPath("$[0].nomenclatureName").exists())
                .andExpect(jsonPath("$[0].uom").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_get_stock_balances_by_warehouse() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse1 = createWarehouseAndReturnDto("Склад №1");
        WarehouseDto warehouse2 = createWarehouseAndReturnDto("Склад №2");

        StorageLocationDto loc1 = createStorageLocationAndReturnDto(warehouse1.getId(), "A", "1", "1", "A-1-1", true);
        StorageLocationDto loc2 = createStorageLocationAndReturnDto(warehouse2.getId(), "B", "2", "1", "B-2-1", true);

        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BAL-002", "Товар 2", InventoryType.QUANTITY);

        createReceiptForWriteOff(nomenclature, warehouse1, loc1, supplier, null);
        createReceiptForWriteOff(nomenclature, warehouse2, loc2, supplier, null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stock-balances")
                        .param("warehouseId", String.valueOf(warehouse1.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].warehouseName", hasItem(warehouse1.getName())))
                .andExpect(jsonPath("$[*].warehouseName", not(hasItem(warehouse2.getName()))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_only_positive_balances_are_returned() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №3");
        StorageLocationDto location = createStorageLocationAndReturnDto(warehouse.getId(), "C", "3", "1", "C-3-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BAL-003", "Товар 3", InventoryType.QUANTITY);

        createReceiptForWriteOff(nomenclature, warehouse, location, supplier, null);

        createIssueMovement(nomenclature, warehouse, location, supplier, BigDecimal.ONE, null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stock-balances")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_dto_structure_and_fields() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №4");
        StorageLocationDto location = createStorageLocationAndReturnDto(warehouse.getId(), "D", "4", "1", "D-4-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BAL-004", "Товар 4", InventoryType.QUANTITY);

        createReceiptForWriteOff(nomenclature, warehouse, location, supplier, null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/stock-balances")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(emptyArray()))))
                .andExpect(jsonPath("[0].sku", not(isEmptyString())))
                .andExpect(jsonPath("[0].nomenclatureName", not(isEmptyString())))
                .andExpect(jsonPath("[0].warehouseName", not(isEmptyString())))
                .andExpect(jsonPath("[0].quantity", notNullValue()))
                .andExpect(jsonPath("[0].uom", notNullValue()));
    }

    private void prepareUser(String username, String role) throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
        jdbcTemplate.update("""
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 10, username, encodedPassword, username + "@test.com", "Test User", role, true);
    }

    private NomenclatureDto createNomenclatureAndReturnDto(String sku, String name, InventoryType inventoryType) throws Exception {
        var dto = new NomenclatureDto();
        dto.setName(name);
        dto.setSku(sku);
        dto.setStuffCategory(StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(inventoryType);
        dto.setActive(true);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(r -> System.out.println("Created nomenclature: " + r.getResponse().getContentAsString()))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), NomenclatureDto.class);
    }

    private WarehouseDto createWarehouseAndReturnDto(String name) throws Exception {
        var dto = new WarehouseDto();
        dto.setName(name);
        dto.setActive(true);
        dto.setAddress("Адрес");
        dto.setType(WarehouseType.REMOTE);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(r -> System.out.println("Created warehouse: " + r.getResponse().getContentAsString()))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), WarehouseDto.class);
    }

    private StorageLocationDto createStorageLocationAndReturnDto(Long warehouseId, String zone, String rack, String shelf,
                                                                 String code, boolean used) throws Exception {
        var dto = new StorageLocationDto();
        dto.setWarehouseId(warehouseId);
        dto.setZone(zone);
        dto.setRack(rack);
        dto.setShelf(shelf);
        dto.setCode(code);
        dto.setUsed(used);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(r -> System.out.println("Created storage location: " + r.getResponse().getContentAsString()))
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
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(r -> System.out.println("Created counterparty: " + r.getResponse().getContentAsString()))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CounterpartyDto.class);
    }

    private MovementDtoReceipt createReceiptForWriteOff(
            NomenclatureDto nomenclature,
            WarehouseDto warehouse,
            StorageLocationDto location,
            CounterpartyDto counterparty,
            String serialNumber) throws Exception {
        var receiptDto = new MovementDtoReceipt();
        receiptDto.setMovementType(MovementType.RECEIPT);
        receiptDto.setNomenclatureId(nomenclature.getId());
        receiptDto.setWarehouseTo(warehouse.getId());
        receiptDto.setStorageLocationId(location.getId());
        receiptDto.setCounterpartyId(counterparty.getId());
        receiptDto.setQuantity(BigDecimal.ONE);
        receiptDto.setPrice(BigDecimal.valueOf(20000));

        String receiptJson = objectMapper.writeValueAsString(receiptDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiptJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MovementDtoReceipt.class);
    }

    private void createIssueMovement(NomenclatureDto nomenclature, WarehouseDto warehouse,
                                     StorageLocationDto location, CounterpartyDto counterparty,
                                     BigDecimal quantity, String serialNumber) throws Exception {
        var issueDto = new MovementDtoIssue();
        issueDto.setMovementType(MovementType.ISSUE);
        issueDto.setNomenclatureId(nomenclature.getId());
        issueDto.setWarehouseFrom(warehouse.getId());
        issueDto.setStorageLocationId(location.getId());
        issueDto.setCustomerId(counterparty.getId());
        issueDto.setQuantity(quantity);
        issueDto.setPrice(BigDecimal.TEN);

        String issueJson = objectMapper.writeValueAsString(issueDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueJson))
                .andDo(print())
                .andExpect(status().isCreated());
    }

}
