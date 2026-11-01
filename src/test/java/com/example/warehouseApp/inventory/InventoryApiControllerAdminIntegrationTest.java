package com.example.warehouseApp.inventory;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.StorageLocationDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.inventory.InventoryCheckCreateDto;
import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.model.DocumentStatus;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.MovementType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class InventoryApiControllerAdminIntegrationTest {

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
    void admin_createCheck_success_withLines() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №1");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "A", "1", "1", "A-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-001", "Товар для инвентаризации", InventoryType.QUANTITY);

        createReceipt(nomen, warehouse, loc, supplier, null);

        String requestJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));

        mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.warehouseId").value(warehouse.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalLines").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_createCheck_emptyWarehouse_returnsZeroLines() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад пустой");

        String requestJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));

        mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.totalLines").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_updateActualQty_success() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №2");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "B", "2", "1", "B-2-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-002", "Товар 2", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        BigDecimal newQty = new BigDecimal("3");

        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", newQty.toPlainString())
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualQty").value(newQty.toPlainString()))
                .andExpect(jsonPath("$.accountedQty").value("1.0"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_updateActualQty_afterComplete_throwsBusinessException() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №3");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "C", "3", "1", "C-3-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-003", "Товар 3", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines"))
                .andExpect(status().isOk())
                .andReturn();

        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        BigDecimal newQty = new BigDecimal("5");

        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", newQty.toPlainString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", "10")
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_completeCheck_successWithDiscrepancies() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №4");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "D", "4", "1", "D-4-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-004", "Товар 4", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/checks/" + checkDto.getId() + "/lines")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        BigDecimal actualQty = new BigDecimal("3");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", actualQty.toPlainString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult completeResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        InventoryDiscrepancyActDto actDto = objectMapper.readValue(completeResult.getResponse().getContentAsString(), InventoryDiscrepancyActDto.class);

        log.info(actDto.toString());
        assertThat(actDto.getId()).isNotNull();
        assertThat(actDto.getCheckId()).isEqualTo(checkDto.getId());
        assertThat(actDto.getStatus()).isEqualTo(DocumentStatus.DRAFT);

    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_completeCheck_noDiscrepancies_success() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №5");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "E", "5", "1", "E-5-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-005", "Товар без расхождений", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines"))
                .andExpect(status().isOk())
                .andReturn();

        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", lines.get(0).getAccountedQty().toPlainString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult completeResult = mockMvc.perform(post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        InventoryDiscrepancyActDto actDto = objectMapper.readValue(completeResult.getResponse().getContentAsString(), InventoryDiscrepancyActDto.class);

        assertThat(actDto.getTotalLines()).isEqualTo(0L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_settleAct_success_createsDocuments() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №6");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "F", "6", "1", "F-6-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-INV-006", "Товар для урегулирования", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines"))
                .andExpect(status().isOk())
                .andReturn();

        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", "0")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult completeResult = mockMvc.perform(post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        InventoryDiscrepancyActDto actDto = objectMapper.readValue(completeResult.getResponse().getContentAsString(), InventoryDiscrepancyActDto.class);
        Long actId = actDto.getId();

        mockMvc.perform(post("/api/inventory/acts/" + actId + "/settle")
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        var writeOffCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM write_off_document WHERE act_id = ?", Integer.class, actId);
        var receiptCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM receipt_document WHERE act_id = ?", Integer.class, actId);

        assertThat(writeOffCount).isEqualTo(1);
        assertThat(receiptCount).isEqualTo(0);
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
        dto.setStuffCategory(com.example.warehouseApp.model.StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(com.example.warehouseApp.model.UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(inventoryType);
        dto.setActive(true);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/nomenclature")
                        .contentType(APPLICATION_JSON)
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
        dto.setType(com.example.warehouseApp.model.WarehouseType.REMOTE);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/warehouse")
                        .contentType(APPLICATION_JSON)
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
        MvcResult result = mockMvc.perform(post("/api/storageLocation")
                        .contentType(APPLICATION_JSON)
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
        dto.setCounterpartyType(com.example.warehouseApp.model.CounterpartyType.SUPPLIER);
        dto.setIsActive(true);
        dto.setContact("contact");
        dto.setPhone("89999999999");

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andDo(r -> System.out.println("Created counterparty: " + r.getResponse().getContentAsString()))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), CounterpartyDto.class);
    }

    private void createReceipt(
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
        MvcResult result = mockMvc.perform(post("/api/receipt")
                        .contentType(APPLICATION_JSON)
                        .content(receiptJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        objectMapper.readValue(result.getResponse().getContentAsString(), MovementDtoReceipt.class);
    }

}
