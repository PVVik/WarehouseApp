package com.example.warehouseApp.serialItem;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.model.InventoryType;
import com.example.warehouseApp.model.ItemStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class SerialItemApiControllerIntegrationTest {

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
    void shouldCreateSerialItemSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-SERIAL-001", "Материал для серийника");
        BatchDto batch = createBatchAndReturnDto(nomenclature.getId(), "BATCH-SERIAL-001");

        var dto = new SerialItemDto();
        dto.setNomenclatureId(nomenclature.getId());
        dto.setSerialNumber("SN-001-TEST");
        dto.setStatus(ItemStatus.IN_STOCK);
        dto.setBatchId(batch.getId());
        dto.setPassportNumber("PASSPORT-001");
        dto.setNotes("Тестовый серийный номер");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.serialNumber").value("SN-001-TEST"))
                .andExpect(jsonPath("$.nomenclatureId").value(nomenclature.getId()))
                .andExpect(jsonPath("$.batchId").value(batch.getId()));
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldCreateSerialItemWithStorekeeperRole() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-SERIAL-KEEPER-001", "Для кладовщика");
        var dto = new SerialItemDto();
        dto.setNomenclatureId(nomenclature.getId());
        dto.setSerialNumber("SN-KEEPER-001");
        dto.setStatus(ItemStatus.IN_STOCK);
        dto.setPassportNumber("PASSPORT-KEEPER-001");
        dto.setNotes("Серийник от кладовщика");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenSerialNumberIsBlank() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BLANK-001", "Пустой серийный номер");
        var dto = new SerialItemDto();
        dto.setNomenclatureId(nomenclature.getId());
        dto.setSerialNumber("");
        dto.setStatus(ItemStatus.IN_STOCK);
        dto.setPassportNumber("PASSPORT-BLANK-001");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Серийный номер обязателен")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenPassportNumberIsBlank() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-PASSPORT-BLANK-001", "Без паспорта");
        var dto = new SerialItemDto();
        dto.setNomenclatureId(nomenclature.getId());
        dto.setSerialNumber("SN-PASSPORT-BLANK-001");
        dto.setStatus(ItemStatus.IN_STOCK);
        dto.setPassportNumber("");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Номер паспорта обязателен")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateSerialItemSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-UPDATE-SERIAL-001", "Обновляемый материал");
        BatchDto batch = createBatchAndReturnDto(nomenclature.getId(), "BATCH-UPDATE-001");

        SerialItemDto original = createSerialItemAndReturnDto(
                nomenclature.getId(),
                "SN-ORIGINAL-001",
                ItemStatus.IN_STOCK,
                batch.getId(),
                "PASSPORT-ORIGINAL-001",
                "Оригинал"
        );

        original.setStatus(ItemStatus.IN_USE);
        original.setNotes("Обновлено: в перемещении");

        String json = objectMapper.writeValueAsString(original);

        mockMvc.perform(patch("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(original.getId()))
                .andExpect(jsonPath("$.status").value("IN_USE"))
                .andExpect(jsonPath("$.notes").value("Обновлено: в перемещении"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllSerialItemsWithPagination() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-PAGINATION-SERIAL-001", "Пагинация");

        for (int i = 0; i < 25; i++) {
            var dto = new SerialItemDto();
            dto.setNomenclatureId(nomenclature.getId());
            dto.setSerialNumber("SN-" + i);
            dto.setStatus(ItemStatus.IN_STOCK);
            dto.setPassportNumber("PASSPORT-" + i);

            String json = objectMapper.writeValueAsString(dto);
            mockMvc.perform(post("/api/serial-item")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/serial-item?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].serialNumber").value("SN-0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchBySerialNumberPattern() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-SEARCH-SERIAL-001", "Поиск");

        createSerialItemAndReturnDto(nomenclature.getId(), "SN-ALPHA-001", ItemStatus.IN_STOCK, null, "PASSPORT-A", null);
        createSerialItemAndReturnDto(nomenclature.getId(), "SN-BETA-002", ItemStatus.IN_USE, null, "PASSPORT-B", null);

        mockMvc.perform(get("/api/serial-item?search=serialNumber:ALPHA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("IN_STOCK"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetByIdSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-GETBYID-SERIAL-001", "По ID");
        SerialItemDto created = createSerialItemAndReturnDto(
                nomenclature.getId(),
                "SN-GETBYID-001",
                ItemStatus.IN_STOCK,
                null,
                "PASSPORT-GETBYID-001",
                "Для теста"
        );

        mockMvc.perform(get("/api/serial-item/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.serialNumber").value(created.getSerialNumber()));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        prepareUser("viewer", "VIEWER");

        mockMvc.perform(get("/api/serial-item/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteSerialItemSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-DELETE-SERIAL-001", "Удаляемый");
        SerialItemDto created = createSerialItemAndReturnDto(
                nomenclature.getId(),
                "SN-DELETE-001",
                ItemStatus.IN_STOCK,
                null,
                "PASSPORT-DELETE-001",
                "К удалению"
        );

        mockMvc.perform(delete("/api/serial-item/" + created.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/serial-item/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteSerialItemWithStorekeeperRole() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-DELETE-KEEPER-001", "Кладовщик удаляет");
        SerialItemDto created = createSerialItemAndReturnDto(
                nomenclature.getId(),
                "SN-DELETE-KEEPER-001",
                ItemStatus.IN_STOCK,
                null,
                "PASSPORT-KEEPER-001",
                "Кладовщик удаляет"
        );

        mockMvc.perform(delete("/api/serial-item/" + created.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/serial-item/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    private void prepareUser(String username, String role) throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
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

    private BatchDto createBatchAndReturnDto(Long nomenclatureId, String batchNumber) throws Exception {
        var dto = new BatchDto();
        dto.setNomenclatureId(nomenclatureId);
        dto.setBatchNumber(batchNumber);
        dto.setProductionDate(LocalDate.now().minusDays(10));
        dto.setExpiryDate(LocalDate.now().plusDays(30));
        dto.setReceiptDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), BatchDto.class);
    }

    private SerialItemDto createSerialItemAndReturnDto(
            Long nomenclatureId,
            String serialNumber,
            ItemStatus status,
            Long batchId,
            String passportNumber,
            String notes
    ) throws Exception {
        var dto = new SerialItemDto();
        dto.setNomenclatureId(nomenclatureId);
        dto.setSerialNumber(serialNumber);
        dto.setStatus(status);
        dto.setBatchId(batchId);
        dto.setPassportNumber(passportNumber);
        dto.setNotes(notes);

        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/serial-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), SerialItemDto.class);
    }

}

