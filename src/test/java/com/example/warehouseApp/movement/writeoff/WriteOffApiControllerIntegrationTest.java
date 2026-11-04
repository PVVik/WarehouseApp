package com.example.warehouseApp.movement.writeoff;

import com.example.warehouseApp.dto.*;
import com.example.warehouseApp.dto.movement.MovementDtoReceipt;
import com.example.warehouseApp.dto.movement.MovementDtoWriteOff;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

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
class WriteOffApiControllerIntegrationTest {

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
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateWriteOffWithSerialItem() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-WRITE-OFF-SERIAL-001", "Оборудование (серийное)");
        nomenclature.setInventoryType(InventoryType.SERIAL);

        WarehouseDto warehouse = createWarehouseAndReturnDto("Склад №1 (для списания)");
        StorageLocationDto location = createStorageLocationAndReturnDto(warehouse.getId(), "B", "2", "1", "B-2-1", true);
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "0987654321", "098765432");

        var receiptResult = createReceiptForWriteOff(nomenclature, warehouse, location, supplier, "SN-WRITE-OFF-001");
        Long createdSerialItemId = receiptResult.getSerialItemDto().getId();

        var writeOffDto = new MovementDtoWriteOff();
        writeOffDto.setMovementType(MovementType.WRITE_OFF);
        writeOffDto.setNomenclatureId(nomenclature.getId());
        writeOffDto.setSerialItemId(createdSerialItemId);
        writeOffDto.setQuantity(BigDecimal.ONE);
        writeOffDto.setWarehouseFrom(warehouse.getId());
        writeOffDto.setStorageLocationId(location.getId());
        writeOffDto.setNotes("Тестовый акт списания серийного оборудования");

        String writeOffJson = objectMapper.writeValueAsString(writeOffDto);

        mockMvc.perform(post("/api/writeoff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeOffJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.serialItemId").value(createdSerialItemId))
                .andExpect(jsonPath("$.quantity").value(1.0))
                .andExpect(jsonPath("$.warehouseFrom").value(warehouse.getId()))
                .andExpect(jsonPath("$.notes").value("Тестовый акт списания серийного оборудования"));
    }

    private MovementDtoReceipt createReceiptForWriteOff(
            NomenclatureDto nomenclature,
            WarehouseDto warehouse,
            StorageLocationDto location,
            CounterpartyDto counterparty,
            String serialNumber) throws Exception {

        SerialItemDto serialItemDto = new SerialItemDto();
        serialItemDto.setSerialNumber(serialNumber);
        serialItemDto.setPassportNumber("PASSPORT-" + serialNumber);
        serialItemDto.setStatus(ItemStatus.IN_STOCK);

        var receiptDto = new MovementDtoReceipt();
        receiptDto.setMovementType(MovementType.RECEIPT);
        receiptDto.setNomenclatureId(nomenclature.getId());
        receiptDto.setSerialItemDto(serialItemDto);
        receiptDto.setWarehouseTo(warehouse.getId());
        receiptDto.setStorageLocationId(location.getId());
        receiptDto.setCounterpartyId(counterparty.getId());
        receiptDto.setQuantity(BigDecimal.ONE);
        receiptDto.setPrice(BigDecimal.valueOf(20000));

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
                .andDo(print())
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
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 10, username, encodedPassword, username + "@test.com", "Test User", role, true);
    }
}
