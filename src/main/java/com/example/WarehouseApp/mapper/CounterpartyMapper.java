package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.model.Counterparty;
import org.flywaydb.core.internal.util.StringUtils;

public class CounterpartyMapper {

    public static Counterparty mapToEntity(CounterpartyDto counterpartyDto) {
        return new Counterparty(counterpartyDto.getName(), counterpartyDto.getInn(), counterpartyDto.getKpp(),
                counterpartyDto.getCounterpartyType(), counterpartyDto.getPhone(), counterpartyDto.getContact(),
                counterpartyDto.getIsActive());
    }

    public static Counterparty mapToEntityWithId(CounterpartyDto counterpartyDto) {
        return new Counterparty(counterpartyDto.getId(), counterpartyDto.getName(), counterpartyDto.getInn(), counterpartyDto.getKpp(),
                counterpartyDto.getCounterpartyType(), counterpartyDto.getPhone(), counterpartyDto.getContact(),
                counterpartyDto.getIsActive());
    }

    public static CounterpartyDto mapToDto(Counterparty counterparty) {
        return new CounterpartyDto(counterparty.getId(), counterparty.getName(), counterparty.getInn(),
                counterparty.getKpp(), counterparty.getCounterpartyType(), counterparty.getPhone(),
                counterparty.getContact(), counterparty.isActive(), counterparty.getCounterpartyType().getLabel());
    }

    public static Counterparty mapToUpdateEntity(Counterparty counterparty, CounterpartyDto counterpartyDto) {
        if (StringUtils.hasText(counterpartyDto.getName())) {
            counterparty.setName(counterpartyDto.getName());
        }
        if (StringUtils.hasText(counterpartyDto.getInn())) {
            counterparty.setInn(counterpartyDto.getInn());
        }
        if (StringUtils.hasText(counterpartyDto.getKpp())) {
            counterparty.setKpp(counterpartyDto.getKpp());
        }
        if (counterpartyDto.getCounterpartyType() != null) {
            counterparty.setCounterpartyType(counterpartyDto.getCounterpartyType());
        }
        if (StringUtils.hasText(counterpartyDto.getPhone())) {
            counterparty.setPhone(counterpartyDto.getPhone());
        }
        if (StringUtils.hasText(counterpartyDto.getContact())) {
            counterparty.setContact(counterpartyDto.getContact());
        }
        if (counterpartyDto.getIsActive() != null) {
            counterparty.setActive(counterpartyDto.getIsActive());
        }

        return counterparty;
    }
}
