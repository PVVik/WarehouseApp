package com.example.warehouseApp.nomenclature;

import com.example.warehouseApp.dto.NomenclatureDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
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
@ActiveProfiles("test")
class NomenclatureApiControllerIntegrationTest {

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
    void shouldCreateNomenclatureSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidNomenclatureDto();

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Новая позиция"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldCreateNomenclatureWithStorekeeperRole() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        var dto = createValidNomenclatureDto();
        dto.setSku("SKU-KEEPER-001");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldRejectCreateWhenUserHasOnlyViewerRole() throws Exception {
        prepareUser("viewer", "VIEWER");

        var dto = createValidNomenclatureDto();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenSkuIsEmpty() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidNomenclatureDto();
        dto.setSku("");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("sku")))
                .andExpect(content().string(containsString("Артикул (SKU) обязателен для заполнения")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenSkuAlreadyExists() throws Exception {
        prepareUser("admin", "ADMIN");

        var firstDto = createValidNomenclatureDto();
        firstDto.setSku("DUPLICATE-SKU");
        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstDto)))
                .andExpect(status().isCreated());

        var secondDto = createValidNomenclatureDto();
        secondDto.setSku("DUPLICATE-SKU");

        mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("уже существует")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateNomenclatureSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var createDto = createValidNomenclatureDto();
        createDto.setSku("SKU-UPDATE-001");
        MvcResult createResult = mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                NomenclatureDto.class
        ).getId();

        var updateDto = createValidNomenclatureDto();
        updateDto.setId(id);
        updateDto.setName("Обновленное название");
        updateDto.setMinStock(BigDecimal.valueOf(50));

        String json = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Обновленное название"))
                .andExpect(jsonPath("$.minStock").value(50.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllNomenclaturesWithPagination() throws Exception {
        prepareUser("admin", "ADMIN");

        for (int i = 1; i <= 25; i++) {
            var dto = createValidNomenclatureDto();
            dto.setSku("SKU-" + i);
            dto.setName("Item " + i);
            mockMvc.perform(post("/api/nomenclature")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/nomenclature?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1")) // проверяем сортировку
                .andExpect(jsonPath("$.content[-1].sku").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchByCustomPattern() throws Exception {
        prepareUser("admin", "ADMIN");

        var d1 = createValidNomenclatureDto();
        d1.setSku("SKU-A");
        d1.setName("Alpha");
        var d2 = createValidNomenclatureDto();
        d2.setSku("SKU-B");
        d2.setName("Beta");
        var d3 = createValidNomenclatureDto();
        d3.setSku("SKU-C");
        d3.setName("Gamma");

        Stream.of(d1, d2, d3).forEach(d -> {
            try {
                mockMvc.perform(post("/api/nomenclature")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(d)))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(get("/api/nomenclature?search=name:Al")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alpha"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetByIdSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidNomenclatureDto();
        MvcResult result = mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                NomenclatureDto.class
        ).getId();

        mockMvc.perform(get("/api/nomenclature/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sku").value(dto.getSku()));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        prepareUser("viewer", "VIEWER");

        mockMvc.perform(get("/api/nomenclature/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteNomenclatureSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidNomenclatureDto();
        MvcResult result = mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                NomenclatureDto.class
        ).getId();

        mockMvc.perform(delete("/api/nomenclature/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/nomenclature/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldRejectDeleteWhenUserIsNotAdmin() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        var dto = createValidNomenclatureDto();
        MvcResult result = mockMvc.perform(post("/api/nomenclature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                NomenclatureDto.class
        ).getId();

        mockMvc.perform(delete("/api/nomenclature/" + id))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    private NomenclatureDto createValidNomenclatureDto() {
        var dto = new NomenclatureDto();
        dto.setName("Новая позиция");
        dto.setSku("SKU-001");
        dto.setStuffCategory(StuffCategory.MATERIAL);
        dto.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
        dto.setInventoryType(InventoryType.BATCH);
        dto.setActive(true);
        dto.setMinStock(BigDecimal.TEN);
        return dto;
    }

    private void prepareUser(String username, String role) throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
        jdbcTemplate.update("""
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 1, username, encodedPassword, username + "@test.com", "Test User", role, true);
    }
}
