package com.example.warehouseApp.storageLocation;

import com.example.warehouseApp.dto.StorageLocationDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.startsWith;
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
class StorageLocationApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldCreateStorageLocationSuccessfully() throws Exception {
        prepareReferenceData();

        var dto = new StorageLocationDto();
        dto.setWarehouseId(1L);
        dto.setZone("A");
        dto.setRack("1");
        dto.setShelf("1");
        dto.setCode("A-1-1");
        dto.setUsed(false);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.warehouseId").value(1))
                .andExpect(jsonPath("$.zone").value("A"))
                .andExpect(jsonPath("$.rack").value("1"))
                .andExpect(jsonPath("$.shelf").value("1"))
                .andExpect(jsonPath("$.code").value("A-1-1"))
                .andExpect(jsonPath("$.used").value(false))
                .andExpect(jsonPath("$.warehouseName").value("Central Warehouse"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenWarehouseIdIsMissing() throws Exception {
        prepareReferenceData();

        var dto = new StorageLocationDto();
        dto.setZone("D");
        dto.setRack("4");
        dto.setShelf("6");
        dto.setCode("D-4-6");
        dto.setUsed(false);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("Validation failed for argument [0]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetStorageLocationByIdSuccessfully() throws Exception {
        prepareReferenceData();

        var createDto = new StorageLocationDto();
        createDto.setWarehouseId(1L);
        createDto.setZone("E");
        createDto.setRack("5");
        createDto.setShelf("7");
        createDto.setCode("E-5-7");
        createDto.setUsed(true);

        String createJson = objectMapper.writeValueAsString(createDto);
        MvcResult result = mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                StorageLocationDto.class
        ).getId();

        mockMvc.perform(get("/api/storageLocation/" + createdId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.code").value("E-5-7"))
                .andExpect(jsonPath("$.used").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenStorageLocationNotFound() throws Exception {
        prepareReferenceData();

        mockMvc.perform(get("/api/storageLocation/9999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateStorageLocationSuccessfully() throws Exception {
        prepareReferenceData();

        // Создаем ячейку
        var createDto = new StorageLocationDto();
        createDto.setWarehouseId(1L);
        createDto.setZone("F");
        createDto.setRack("6");
        createDto.setShelf("8");
        createDto.setCode("F-6-8");
        createDto.setUsed(false);

        String createJson = objectMapper.writeValueAsString(createDto);
        MvcResult createResult = mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        StorageLocationDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                StorageLocationDto.class
        );

        created.setUsed(true);
        created.setShelf("9");
        created.setCode("F-6-9");

        String updateJson = objectMapper.writeValueAsString(created);

        mockMvc.perform(patch("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.used").value(true))
                .andExpect(jsonPath("$.shelf").value("9"))
                .andExpect(jsonPath("$.code").value("F-6-9"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteStorageLocationSuccessfully() throws Exception {
        prepareReferenceData();

        var createDto = new StorageLocationDto();
        createDto.setWarehouseId(1L);
        createDto.setZone("G");
        createDto.setRack("7");
        createDto.setShelf("10");
        createDto.setCode("G-7-10");
        createDto.setUsed(false);

        String createJson = objectMapper.writeValueAsString(createDto);
        MvcResult createResult = mockMvc.perform(post("/api/storageLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        StorageLocationDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                StorageLocationDto.class
        );

        mockMvc.perform(delete("/api/storageLocation/" + created.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/storageLocation/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListStorageLocationsWithPaginationAndSearch() throws Exception {
        prepareReferenceData();

        for (int i = 1; i <= 3; i++) {
            var dto = new StorageLocationDto();
            dto.setWarehouseId(1L);
            dto.setZone("H");
            dto.setRack(String.valueOf(i));
            dto.setShelf(String.valueOf(i * 10));
            dto.setCode("H-" + i + "-" + (i * 10));
            dto.setUsed(i % 2 == 0);
            String json = objectMapper.writeValueAsString(dto);
            mockMvc.perform(post("/api/storageLocation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/storageLocation?page=0&size=10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].code").exists())
                .andExpect(jsonPath("$.content[-1].code").exists());
    }

    private void prepareReferenceData() throws Exception {
        createWarehouse("Central Warehouse", "Addr Central", true);
        createWarehouse("Remote Warehouse", "Addr Remote", true);
    }

    private void createWarehouse(String name, String address, boolean active) throws Exception {
        var dto = new WarehouseDto();
        dto.setName(name);
        dto.setAddress(address);
        dto.setType(WarehouseType.CENTRAL);
        dto.setActive(active);

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/warehouse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}
