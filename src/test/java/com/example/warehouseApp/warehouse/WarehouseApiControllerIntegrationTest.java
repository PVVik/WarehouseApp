package com.example.warehouseApp.warehouse;

import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.model.WarehouseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WarehouseApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase(@Autowired Flyway flywayInstance) {
        flywayInstance.clean();
        flywayInstance.migrate();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateWarehouseAndReturnCreated() throws Exception {
        WarehouseDto dto = new WarehouseDto();
        dto.setName("Test Warehouse 1");
        dto.setAddress("Test Address 1");
        dto.setType(WarehouseType.CENTRAL);
        dto.setActive(true);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Warehouse 1"))
                .andExpect(jsonPath("$.address").value("Test Address 1"))
                .andExpect(jsonPath("$.type").value("CENTRAL"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
        WarehouseDto dto = new WarehouseDto();
        dto.setName("");
        dto.setAddress("Some Address");
        dto.setType(WarehouseType.REMOTE);
        dto.setActive(false);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateWarehouse() throws Exception {
        // 1. Создаём
        WarehouseDto createDto = new WarehouseDto();
        createDto.setName("ToUpdate");
        createDto.setAddress("Before Update");
        createDto.setType(WarehouseType.REMOTE);
        createDto.setActive(true);
        String createJson = objectMapper.writeValueAsString(createDto);

        var result = mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Long id = objectMapper.readTree(content).get("id").asLong();

        WarehouseDto updateDto = new WarehouseDto();
        updateDto.setId(id);
        updateDto.setName("Updated Name");
        updateDto.setAddress("After Update");
        updateDto.setType(WarehouseType.REMOTE);
        updateDto.setActive(false);
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(patch("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.address").value("After Update"))
                .andExpect(jsonPath("$.type").value("REMOTE"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteWarehouse() throws Exception {
        WarehouseDto dto = new WarehouseDto();
        dto.setName("ToDelete");
        dto.setAddress("Address to delete");
        dto.setType(WarehouseType.REMOTE);
        dto.setActive(true);
        String json = objectMapper.writeValueAsString(dto);

        var result = mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Long id = objectMapper.readTree(content).get("id").asLong();

        mockMvc.perform(delete("/api/warehouse/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/warehouse/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetWarehousesListWithSearch() throws Exception {
        createWarehouse("Search Test 1", "Addr 1", WarehouseType.REMOTE, true);
        createWarehouse("Search Test 2", "Addr 2", WarehouseType.CENTRAL, false);

        mockMvc.perform(get("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Search Test 1"))
                .andExpect(jsonPath("$.content[1].name").value("Search Test 2"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldListWarehousesButNotCreate() throws Exception {
        mockMvc.perform(get("/api/warehouse"))
                .andDo(print())
                .andExpect(status().isOk());

        WarehouseDto dto = new WarehouseDto();
        dto.setName("Not Allowed");
        dto.setAddress("Addr");
        dto.setType(WarehouseType.REMOTE);
        dto.setActive(true);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenGettingNonExistingWarehouse() throws Exception {
        mockMvc.perform(get("/api/warehouse/999999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void createWarehouse(String name, String address, WarehouseType type, boolean active) throws Exception {
        WarehouseDto dto = new WarehouseDto();
        dto.setName(name);
        dto.setAddress(address);
        dto.setType(type);
        dto.setActive(active);
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

}
