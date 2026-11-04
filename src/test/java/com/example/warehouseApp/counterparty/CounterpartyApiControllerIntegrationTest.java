package com.example.warehouseApp.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.model.CounterpartyType;
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
class CounterpartyApiControllerIntegrationTest {

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
    void shouldCreateCounterpartySuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidCounterpartyDto();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("ООО «Поставщик №1»"))
                .andExpect(jsonPath("$.inn").value("1234567890"))
                .andExpect(jsonPath("$.kpp").value("123456789"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldCreateCounterpartyWithStorekeeperRole() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        var dto = createValidCounterpartyDto();
        dto.setName("ООО «Складской поставщик»");
        dto.setInn("0987654321");
        dto.setKpp("987654321");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldRejectCreateWhenUserHasOnlyViewerRole() throws Exception {
        prepareUser("viewer", "VIEWER");

        var dto = createValidCounterpartyDto();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenInnIsInvalidLength() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidCounterpartyDto();
        dto.setInn("12345"); // не 10 и не 12 символов

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ИНН должен содержать только цифры и быть длиной 10 или 12 символов")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenKppIsNotNineDigits() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidCounterpartyDto();
        dto.setKpp("123456");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("КПП должен содержать ровно 9 цифр")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenInnAlreadyExists() throws Exception {
        prepareUser("admin", "ADMIN");

        var firstDto = createValidCounterpartyDto();
        firstDto.setInn("9999999990");
        String firstJson = objectMapper.writeValueAsString(firstDto);
        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                .andExpect(status().isCreated());

        var secondDto = createValidCounterpartyDto();
        secondDto.setInn("9999999990");
        String secondJson = objectMapper.writeValueAsString(secondDto);

        mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Контрагент с ИНН 9999999990 уж существует")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCounterpartySuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var createDto = createValidCounterpartyDto();
        createDto.setInn("8888888880");
        String createJson = objectMapper.writeValueAsString(createDto);
        MvcResult createResult = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CounterpartyDto.class
        ).getId();

        var updateDto = createValidCounterpartyDto();
        updateDto.setId(id);
        updateDto.setName("ООО «Обновлённый поставщик»");
        updateDto.setPhone("+79991112233");
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("ООО «Обновлённый поставщик»"))
                .andExpect(jsonPath("$.phone").value("+79991112233"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllCounterpartiesWithPagination() throws Exception {
        prepareUser("admin", "ADMIN");

        for (int i = 0; i <= 9; i++) {
            var dto = createValidCounterpartyDto();
            dto.setInn("111111111" + i);
            dto.setName("Контрагент №" + i);
            String json = objectMapper.writeValueAsString(dto);
            mockMvc.perform(post("/api/counterparty")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/counterparty?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchByCustomPattern() throws Exception {
        prepareUser("admin", "ADMIN");

        var d1 = createValidCounterpartyDto();
        d1.setInn("5555555550");
        d1.setName("Альфа-Контрагент");
        var d2 = createValidCounterpartyDto();
        d2.setInn("6666666660");
        d2.setName("Бета-Контрагент");

        Stream.of(d1, d2).forEach(d -> {
            try {
                mockMvc.perform(post("/api/counterparty")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(d)))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(get("/api/counterparty?search=name:Альфа")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Альфа-Контрагент"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetByIdSuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidCounterpartyDto();
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CounterpartyDto.class
        ).getId();

        mockMvc.perform(get("/api/counterparty/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.inn").value("1234567890"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        prepareUser("viewer", "VIEWER");

        mockMvc.perform(get("/api/counterparty/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCounterpartySuccessfully() throws Exception {
        prepareUser("admin", "ADMIN");

        var dto = createValidCounterpartyDto();
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CounterpartyDto.class
        ).getId();

        mockMvc.perform(delete("/api/counterparty/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/counterparty/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    void shouldRejectDeleteWhenUserIsNotAdmin() throws Exception {
        prepareUser("keeper", "STOREKEEPER");

        var dto = createValidCounterpartyDto();
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/api/counterparty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CounterpartyDto.class
        ).getId();

        mockMvc.perform(delete("/api/counterparty/" + id))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    private CounterpartyDto createValidCounterpartyDto() {
        var dto = new CounterpartyDto();
        dto.setName("ООО «Поставщик №1»");
        dto.setInn("1234567890");
        dto.setKpp("123456789");
        dto.setCounterpartyType(CounterpartyType.SUPPLIER);
        dto.setPhone("+79001112233");
        dto.setContact("Иванов И.И.");
        dto.setIsActive(true);
        return dto;
    }

    private void prepareUser(String username, String role) throws Exception {
        String encodedPassword = "$2a$10$dXJ3SW6G7P50lGmELsHYdeRR";
        jdbcTemplate.update("""
                INSERT INTO users (id, username, password, email, full_name, role, active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 10, username, encodedPassword, username + "@test.com", "Test User", role, true);
    }
}
