package com.example.WarehouseApp.nomenclature;

import com.example.WarehouseApp.controller.NomenclatureController;
import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.exception.ErrorHandler;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.model.InventoryType;
import com.example.WarehouseApp.model.StuffCategory;
import com.example.WarehouseApp.model.UnitOfMeasure;
import com.example.WarehouseApp.service.BaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NomenclatureController.class)
@Import(ErrorHandler.class)
class NomenclatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BaseService<NomenclatureDto> nomenclatureService;

    @Autowired
    private ObjectMapper objectMapper;

    private NomenclatureDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = new NomenclatureDto();
        sampleDto.setId(1L);
        sampleDto.setName("Болт М8 оцинкованный");
        sampleDto.setSku("BLT-M8-ZN-001");
        sampleDto.setStuffCategory(StuffCategory.MATERIAL);
        sampleDto.setUnitOfMeasure(UnitOfMeasure.PIECE);
        sampleDto.setInventoryType(InventoryType.QUANTITY);
        sampleDto.setActive(true);
    }

    @Test
    void addNomenclature_shouldReturn201AndDto() throws Exception {
        when(nomenclatureService.create(any(NomenclatureDto.class)))
                .thenReturn(sampleDto);

        mockMvc.perform(post("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Болт М8 оцинкованный"))
                .andExpect(jsonPath("$.sku").value("BLT-M8-ZN-001"))
                .andExpect(jsonPath("$.stuffCategory").value("MATERIAL"))
                .andExpect(jsonPath("$.unitOfMeasure").value("PIECE"))
                .andExpect(jsonPath("$.inventoryType").value("QUANTITY"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void addNomenclature_shouldReturn400_whenNameBlank() throws Exception {
        var invalid = new NomenclatureDto();
        invalid.setName("");
        invalid.setSku("SKU-001");
        invalid.setStuffCategory(StuffCategory.MATERIAL);
        invalid.setUnitOfMeasure(UnitOfMeasure.PIECE);
        invalid.setInventoryType(InventoryType.QUANTITY);
        invalid.setActive(true);

        mockMvc.perform(post("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNomenclature_shouldReturn400_whenSkuBlank() throws Exception {
        var invalid = new NomenclatureDto();
        invalid.setName("Болт");
        invalid.setSku("");
        invalid.setStuffCategory(StuffCategory.MATERIAL);
        invalid.setUnitOfMeasure(UnitOfMeasure.PIECE);
        invalid.setInventoryType(InventoryType.QUANTITY);
        invalid.setActive(true);

        mockMvc.perform(post("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNomenclature_shouldReturn400_whenStuffCategoryNull() throws Exception {
        var invalid = new NomenclatureDto();
        invalid.setName("Болт");
        invalid.setSku("SKU-001");
        invalid.setStuffCategory(null);
        invalid.setUnitOfMeasure(UnitOfMeasure.PIECE);
        invalid.setInventoryType(InventoryType.QUANTITY);
        invalid.setActive(true);

        mockMvc.perform(post("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNomenclature_shouldReturn400_whenBodyEmpty() throws Exception {
        mockMvc.perform(post("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNomenclature_shouldReturn200AndUpdatedDto() throws Exception {
        sampleDto.setName("Болт М8 (обновлён)");
        sampleDto.setStuffCategory(StuffCategory.EQUIPMENT);
        when(nomenclatureService.update(any(NomenclatureDto.class)))
                .thenReturn(sampleDto);

        mockMvc.perform(patch("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Болт М8 (обновлён)"))
                .andExpect(jsonPath("$.stuffCategory").value("EQUIPMENT"));
    }

    @Test
    void updateNomenclature_shouldReturn404_whenIdNotFound() throws Exception {
        when(nomenclatureService.update(any(NomenclatureDto.class)))
                .thenThrow(new NotFoundException("Позиция с id 99999 не найдена"));

        mockMvc.perform(patch("/nomenclature/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnPageWithDefaultParams() throws Exception {
        var page = new PageImpl<>(List.of(sampleDto));
        when(nomenclatureService.getAll(any(Pageable.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/nomenclature/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Болт М8 оцинкованный"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAll_shouldReturnPageWithCustomPagination() throws Exception {
        var page = new PageImpl<>(
                List.of(sampleDto),
                PageRequest.of(1, 5, Sort.by("name").ascending()),
                10
        );
        when(nomenclatureService.getAll(any(Pageable.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/nomenclature/api")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getAll_shouldReturnFilteredPage_whenSearchProvided() throws Exception {
        var page = new PageImpl<>(List.of(sampleDto));
        when(nomenclatureService.getAll(any(Pageable.class), eq("name:Болт")))
                .thenReturn(page);

        mockMvc.perform(get("/nomenclature/api")
                        .param("search", "name:Болт"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Болт М8 оцинкованный"));

        verify(nomenclatureService).getAll(any(Pageable.class), eq("name:Болт"));
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoResults() throws Exception {
        Page<NomenclatureDto> emptyPage = new PageImpl<>(List.of());
        when(nomenclatureService.getAll(any(Pageable.class), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/nomenclature/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    void getNomenclatureById_shouldReturn200AndDto() throws Exception {
        when(nomenclatureService.getById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/nomenclature/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Болт М8 оцинкованный"))
                .andExpect(jsonPath("$.sku").value("BLT-M8-ZN-001"));
    }

    @Test
    void getNomenclatureById_shouldReturn404_whenNotFound() throws Exception {
        when(nomenclatureService.getById(99999L))
                .thenThrow(new NotFoundException("Позиция с id 99999 не найдена"));

        mockMvc.perform(get("/nomenclature/api/by-id/99999"))
                .andExpect(status().isNotFound());
    }
}

