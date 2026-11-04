package com.example.warehouseApp.documents;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.StorageLocationDto;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.dto.inventory.InventoryCheckCreateDto;
import com.example.warehouseApp.dto.inventory.InventoryCheckDtoResponse;
import com.example.warehouseApp.dto.inventory.InventoryDiscrepancyActDto;
import com.example.warehouseApp.dto.inventory.InventoryLineDto;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.MovementType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
@Slf4j
@ActiveProfiles("test")
public class EndActApiControllerIntegrationTest {

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
    void admin_completeWriteOff_success() throws Exception {
        PreparedActData data = prepareActWithDocument(true, false);

        String reason = "Недостача по инвентаризации";

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .param("reason", reason)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(data.writeOffDocId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.reason").value(reason))
                .andExpect(jsonPath("$.breakdown[*].quantity").isNotEmpty());

        var status = jdbcTemplate.queryForObject(
                "SELECT status FROM write_off_document WHERE id = ?",
                String.class, data.writeOffDocId);
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void storekeeper_completeWriteOff_success() throws Exception {
        PreparedActData data = prepareActWithDocument(true, false);

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .param("reason", "Уценка/списание")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_completeReceipt_success() throws Exception {
        PreparedActData data = prepareActWithDocument(false, true);

        String source = "Перемещение из цеха";

        mockMvc.perform(post("/api/end-act/receipt/" + data.actId + "/complete")
                        .param("source", source)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(data.receiptDocId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.source").value(source))
                .andExpect(jsonPath("$.breakdown[*].quantity").isNotEmpty());

        var status = jdbcTemplate.queryForObject(
                "SELECT status FROM receipt_document WHERE id = ?",
                String.class, data.receiptDocId);
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeWriteOff_missingReason_returnsBadRequest() throws Exception {
        PreparedActData data = prepareActWithDocument(true, false);

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeReceipt_missingSource_returnsBadRequest() throws Exception {
        PreparedActData data = prepareActWithDocument(false, true);

        mockMvc.perform(post("/api/end-act/receipt/" + data.actId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeWriteOff_actNotFound_returnsNotFound() throws Exception {
        prepareUser("admin", "ADMIN");
        long nonExistentActId = 9999L;

        mockMvc.perform(post("/api/end-act/write-off/" + nonExistentActId + "/complete")
                        .param("reason", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeWriteOff_noDocumentForAct_returnsNotFound() throws Exception {
        PreparedActData data = prepareActWithoutDocuments();

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .param("reason", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Не найден документ на списание")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeWriteOff_documentNotInDraft_throwsConflict() throws Exception {
        PreparedActData data = prepareActWithDocument(true, false);

        jdbcTemplate.update("UPDATE write_off_document SET status = 'COMPLETED' WHERE id = ?", data.writeOffDocId);

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .param("reason", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("не в статусе DRAFT")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void completeWriteOff_noWriteOffLinesInAct_throwsIllegalState() throws Exception {
        PreparedActData data = prepareActWithDocumentButNoWriteOffLines();

        mockMvc.perform(post("/api/end-act/write-off/" + data.actId + "/complete")
                        .param("reason", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Не найден документ на списание у акта 1")));
    }

    private PreparedActData prepareActWithDocument(boolean hasWriteOff, boolean hasReceipt) throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад для EndAct");
        StorageLocationDto loc = createStorageLocationAndReturnDto(
                warehouse.getId(), "X", "1", "1", "X-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto(
                "ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto(
                "SKU-EA-001", "Товар для EndAct", InventoryType.QUANTITY);

        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(
                new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines"))
                .andExpect(status().isOk())
                .andReturn();
        List<InventoryLineDto> lines = objectMapper.readValue(
                linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        if (lines.isEmpty()) {
            fail("В инвентаризации нет строк — невозможно создать расхождение");
        }

        Long lineId = lines.get(0).getId();
        BigDecimal accountedQty = lines.get(0).getAccountedQty();

        BigDecimal newQty;
        if (hasWriteOff) {
            newQty = accountedQty.subtract(new BigDecimal("1"));
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                newQty = BigDecimal.ZERO;
            }
        } else if (hasReceipt) {
            newQty = accountedQty.add(new BigDecimal("1"));
        } else {
            newQty = accountedQty;
        }

        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", newQty.toPlainString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult completeResult = mockMvc.perform(
                        post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        InventoryDiscrepancyActDto actDto = objectMapper.readValue(
                completeResult.getResponse().getContentAsString(),
                InventoryDiscrepancyActDto.class);
        Long actId = actDto.getId();

        mockMvc.perform(post("/api/inventory/acts/" + actId + "/settle")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Long writeOffDocId = null;
        Long receiptDocId = null;

        if (hasWriteOff) {
            List<Long> writeOffIds = jdbcTemplate.query(
                    "SELECT id FROM write_off_document WHERE act_id = ?",
                    (rs, rowNum) -> rs.getLong("id"),
                    actId);
            if (writeOffIds.isEmpty()) {
                fail("Для акта " + actId + " не был создан документ списания. " +
                        "Проверь логику settleAct: должны быть строки с actualQty < accountedQty.");
            }
            writeOffDocId = writeOffIds.get(0);
        }

        if (hasReceipt) {
            List<Long> receiptIds = jdbcTemplate.query(
                    "SELECT id FROM receipt_document WHERE act_id = ?",
                    (rs, rowNum) -> rs.getLong("id"),
                    actId);
            if (receiptIds.isEmpty()) {
                fail("Для акта " + actId + " не был создан документ прихода.");
            }
            receiptDocId = receiptIds.get(0);
        }

        return new PreparedActData(actId, writeOffDocId, receiptDocId);
    }


    private PreparedActData prepareActWithoutDocuments() throws Exception {
        prepareUser("admin", "ADMIN");
        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад без документов");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "Y", "1", "1", "Y-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");
        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-EA-002", "Товар без документов", InventoryType.QUANTITY);
        createReceipt(nomen, warehouse, loc, supplier, null);

        String createJson = objectMapper.writeValueAsString(new InventoryCheckCreateDto(warehouse.getId()));
        MvcResult createResult = mockMvc.perform(post("/api/inventory/checks")
                        .param("warehouseId", String.valueOf(warehouse.getId()))
                        .contentType(APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated()).andReturn();

        InventoryCheckDtoResponse checkDto = objectMapper.readValue(createResult.getResponse().getContentAsString(), InventoryCheckDtoResponse.class);

        MvcResult linesResult = mockMvc.perform(get("/api/inventory/checks/" + checkDto.getId() + "/lines"))
                .andExpect(status().isOk()).andReturn();
        List<InventoryLineDto> lines = objectMapper.readValue(linesResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<List<InventoryLineDto>>() {
                });

        Long lineId = lines.get(0).getId();
        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                .param("qty", "3")
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        MvcResult completeResult = mockMvc.perform(post("/api/inventory/checks/" + checkDto.getId() + "/complete")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        InventoryDiscrepancyActDto actDto = objectMapper.readValue(completeResult.getResponse().getContentAsString(), InventoryDiscrepancyActDto.class);

        return new PreparedActData(actDto.getId(), null, null);
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


    private PreparedActData prepareActWithDocumentButNoWriteOffLines() throws Exception {
        prepareUser("admin", "ADMIN");

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад без списаний");
        StorageLocationDto loc = createStorageLocationAndReturnDto(warehouse.getId(), "Z", "1", "1", "Z-1-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        NomenclatureDto nomen = createNomenclatureAndReturnDto("SKU-EA-003", "Товар без списаний", InventoryType.QUANTITY);
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
        BigDecimal accountedQty = lines.get(0).getAccountedQty();

        BigDecimal actualQty = accountedQty.add(new BigDecimal("2"));

        mockMvc.perform(put("/api/inventory/lines/" + lineId + "/actual-qty")
                        .param("qty", actualQty.toPlainString())
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
                .andExpect(status().isNoContent());

        List<Long> writeOffIds = jdbcTemplate.queryForList(
                "SELECT id FROM write_off_document WHERE act_id = ?",
                Long.class,
                actId
        );
        Long writeOffDocId = writeOffIds.isEmpty() ? null : writeOffIds.get(0);

        Long receiptDocId = jdbcTemplate.queryForObject(
                "SELECT id FROM receipt_document WHERE act_id = ?", Long.class, actId);

        return new PreparedActData(actId, writeOffDocId, receiptDocId);
    }

    @Data
    @AllArgsConstructor
    private static class PreparedActData {
        private final Long actId;
        private final Long writeOffDocId;
        private final Long receiptDocId;
    }
}