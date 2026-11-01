package com.example.warehouseApp.batch;

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
class BatchApiControllerIntegrationTest {

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
    void shouldCreateBatchSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BATCH-001", "Материал для партии");

        var batchDto = createValidBatchDto(nomenclature.getId(), null);
        String json = objectMapper.writeValueAsString(batchDto);

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.batchNumber").value("BATCH-001"))
                .andExpect(jsonPath("$.nomenclatureId").value(nomenclature.getId()));
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldCreateBatchWithStorekeeperRole() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BATCH-KEEPER-001", "Материал от кладовщика");
        var batchDto = createValidBatchDto(nomenclature.getId(), null);
        batchDto.setBatchNumber("BATCH-KEEPER-001");

        String json = objectMapper.writeValueAsString(batchDto);

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenProductionDateIsInFuture() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-FUTURE-001", "Будущий материал");
        var batchDto = createValidBatchDto(nomenclature.getId(), null);
        batchDto.setProductionDate(LocalDate.now().plusDays(1));

        String json = objectMapper.writeValueAsString(batchDto);

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Дата производства не может быть в будущем")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenExpiryDateIsInPast() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-PAST-001", "Просроченный материал");
        var batchDto = createValidBatchDto(nomenclature.getId(), null);
        batchDto.setExpiryDate(LocalDate.now().minusDays(1));

        String json = objectMapper.writeValueAsString(batchDto);

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Срок годности не может быть в прошлом")));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenExpiryBeforeProduction() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-BAD-DATE-001", "Некорректные даты");
        var batchDto = createValidBatchDto(nomenclature.getId(), null);

        batchDto.setProductionDate(LocalDate.now().minusDays(10));
        batchDto.setExpiryDate(LocalDate.now().minusDays(20));

        String json = objectMapper.writeValueAsString(batchDto);

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Срок годности не может быть в прошлом")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBatchSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-UPDATE-001", "Обновляемый материал");
        CounterpartyDto supplier = createCounterpartyAndReturnDto("ООО «Поставщик»", "1234567890", "123456789");

        var createDto = createValidBatchDto(nomenclature.getId(), supplier.getId());
        createDto.setBatchNumber("BATCH-ORIGINAL-001");
        String createJson = objectMapper.writeValueAsString(createDto);
        MvcResult createResult = mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BatchDto.class
        ).getId();

        var updateDto = createValidBatchDto(nomenclature.getId(), supplier.getId());
        updateDto.setId(id);
        updateDto.setBatchNumber("BATCH-UPDATED-001");
        updateDto.setStatus("IN_PROGRESS");
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.batchNumber").value("BATCH-UPDATED-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllBatchesWithPagination() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-PAGINATION-001", "Для пагинации");

        for (int i = 0; i < 25; i++) {
            var dto = createValidBatchDto(nomenclature.getId(), null);
            dto.setBatchNumber("BATCH-" + i);
            String json = objectMapper.writeValueAsString(dto);
            mockMvc.perform(post("/api/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/batch?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].batchNumber").value("BATCH-0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchByCustomPattern() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-SEARCH-001", "Для поиска");

        var d1 = createValidBatchDto(nomenclature.getId(), null);
        d1.setBatchNumber("BATCH-ALPHA-001");
        d1.setStatus("ACTIVE");

        var d2 = createValidBatchDto(nomenclature.getId(), null);
        d2.setBatchNumber("BATCH-BETA-002");
        d2.setStatus("ARCHIVED");

        Stream.of(d1, d2).forEach(d -> {
            try {
                String json = objectMapper.writeValueAsString(d);
                mockMvc.perform(post("/api/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(get("/api/batch?search=batchNumber:ALPHA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetByIdSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-GETBYID-001", "Для получения по ID");
        var dto = createValidBatchDto(nomenclature.getId(), null);
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BatchDto.class
        ).getId();

        mockMvc.perform(get("/api/batch/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.batchNumber").value(dto.getBatchNumber()));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        prepareUser("viewer", "VIEWER");

        mockMvc.perform(get("/api/batch/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBatchSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-DELETE-001", "Удаляемый материал");
        var dto = createValidBatchDto(nomenclature.getId(), null);
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BatchDto.class
        ).getId();

        mockMvc.perform(delete("/api/batch/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/batch/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldRejectDeleteWhenUserIsNotAdmin() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        NomenclatureDto nomenclature = createNomenclatureAndReturnDto("SKU-DELETE-KEEPER-001", "Материал для партии");
        var dto = createValidBatchDto(nomenclature.getId(), null);
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BatchDto.class
        ).getId();

        mockMvc.perform(delete("/api/batch/" + id))
                .andDo(print())
                .andExpect(status().isForbidden());
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
