package com.example.WarehouseApp.counterparty;

import com.example.WarehouseApp.controller.counterparty.CounterpartyController;
import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.exception.AlreadyExistsException;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.model.CounterpartyType;
import com.example.WarehouseApp.service.BaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CounterpartyController.class)
class CounterpartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaseService<CounterpartyDto> counterpartyService;

    private CounterpartyDto buildDto(Long id, String name, String inn, String kpp,
                                     CounterpartyType type, String phone, String contact, boolean isActive) {
        CounterpartyDto dto = new CounterpartyDto();
        dto.setId(id);
        dto.setName(name);
        dto.setInn(inn);
        dto.setKpp(kpp);
        dto.setCounterpartyType(type);
        dto.setPhone(phone);
        dto.setContact(contact);
        dto.setIsActive(isActive);
        return dto;
    }

    @Test
    void addCounterpartyApi_shouldReturn201_andJsonBody() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);
        CounterpartyDto response = buildDto(1L, "ООО «Ромашка»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        when(counterpartyService.create(any(CounterpartyDto.class))).thenReturn(response);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ООО «Ромашка»"))
                .andExpect(jsonPath("$.inn").value("7701234567"))
                .andExpect(jsonPath("$.kpp").value("770101001"))
                .andExpect(jsonPath("$.counterpartyType").value("SUPPLIER"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.contact").value("Иванов И.И."))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenNameIsBlank() throws Exception {
        CounterpartyDto request = buildDto(null, "", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenInnIsBlank() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenKppIsBlank() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "7701234567", "",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenPhoneIsInvalid() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "123", "Иванов И.И.", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenContactIsBlank() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenCounterpartyTypeIsNull() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Ромашка»", "7701234567", "770101001",
                null, "+79991234567", "Иванов И.И.", true);

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any());
    }

    @Test
    void addCounterpartyApi_shouldReturn400_whenDuplicateInn() throws Exception {
        CounterpartyDto request = buildDto(null, "ООО «Дубликат»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        when(counterpartyService.create(any(CounterpartyDto.class)))
                .thenThrow(new AlreadyExistsException("Контрагент с ИНН 7701234567 уже существует"));

        mockMvc.perform(post("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(counterpartyService).create(any(CounterpartyDto.class));
    }

    @Test
    void getCounterpartyByIdApi_shouldReturn200_whenExists() throws Exception {
        CounterpartyDto dto = buildDto(1L, "ООО «Ромашка»", "7701234567", "770101001",
                CounterpartyType.CUSTOMER, "+79997654321", "Петров П.П.", false);

        when(counterpartyService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/counterparty/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ООО «Ромашка»"))
                .andExpect(jsonPath("$.inn").value("7701234567"))
                .andExpect(jsonPath("$.counterpartyType").value("CUSTOMER"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void getCounterpartyByIdApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        when(counterpartyService.getById(id))
                .thenThrow(new NotFoundException(String.format("Контрагента с id %d не существует", id)));

        mockMvc.perform(get("/counterparty/api/" + id))
                .andExpect(status().isNotFound());

        verify(counterpartyService).getById(eq(id));
    }

    @Test
    void updateCounterpartyApi_shouldReturn200_andUpdatedBody() throws Exception {
        CounterpartyDto request = buildDto(1L, "ООО «Ромашка (обновлён)»", "7701234567", "770101001",
                CounterpartyType.CUSTOMER, "+79991234567", "Сидоров С.С.", false);
        CounterpartyDto response = buildDto(1L, "ООО «Ромашка (обновлён)»", "7701234567", "770101001",
                CounterpartyType.CUSTOMER, "+79991234567", "Сидоров С.С.", false);

        when(counterpartyService.update(any(CounterpartyDto.class))).thenReturn(response);

        mockMvc.perform(patch("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ООО «Ромашка (обновлён)»"))
                .andExpect(jsonPath("$.contact").value("Сидоров С.С."))
                .andExpect(jsonPath("$.counterpartyType").value("CUSTOMER"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void updateCounterpartyApi_shouldReturn404_whenNotFound() throws Exception {
        CounterpartyDto request = buildDto(999L, "Несуществующий", "0000000000", "000000000",
                CounterpartyType.SUPPLIER, "+70000000000", "Никто Н.Н.", true);

        when(counterpartyService.update(any(CounterpartyDto.class)))
                .thenThrow(new NotFoundException("Контрагента с id 999 не существует"));

        mockMvc.perform(patch("/counterparty/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(counterpartyService).update(any(CounterpartyDto.class));
    }

    @Test
    void getAllCounterpartiesApi_shouldReturn200_andPage() throws Exception {
        CounterpartyDto dto1 = buildDto(1L, "ООО «Поставщик»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);
        CounterpartyDto dto2 = buildDto(2L, "ООО «Заказчик»", "7707654321", "770101002",
                CounterpartyType.CUSTOMER, "+79997654321", "Петров П.П.", false);

        Page<CounterpartyDto> page = new PageImpl<>(List.of(dto1, dto2));

        when(counterpartyService.getAll(any(Pageable.class), eq(null))).thenReturn(page);

        mockMvc.perform(get("/counterparty/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("ООО «Поставщик»"))
                .andExpect(jsonPath("$.content[1].name").value("ООО «Заказчик»"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllCounterpartiesApi_shouldReturn200_withSearch() throws Exception {
        CounterpartyDto dto1 = buildDto(1L, "ООО «Поставщик»", "7701234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true);

        Page<CounterpartyDto> page = new PageImpl<>(List.of(dto1));

        when(counterpartyService.getAll(any(Pageable.class), eq("name:Поставщик,"))).thenReturn(page);

        mockMvc.perform(get("/counterparty/api").param("search", "name:Поставщик,"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("ООО «Поставщик»"));
    }

    @Test
    void deleteCounterpartyApi_shouldReturn204_whenExists() throws Exception {
        long id = 1L;

        doNothing().when(counterpartyService).delete(id);

        mockMvc.perform(delete("/counterparty/api/" + id))
                .andExpect(status().isNoContent());

        verify(counterpartyService).delete(eq(id));
    }

    @Test
    void deleteCounterpartyApi_shouldReturn404_whenNotExists() throws Exception {
        long id = 999L;

        doThrow(new NotFoundException("Контрагента с id 999 не существует"))
                .when(counterpartyService).delete(id);

        mockMvc.perform(delete("/counterparty/api/" + id))
                .andExpect(status().isNotFound());

        verify(counterpartyService).delete(eq(id));
    }
}

