package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.model.SerialItem;
import org.flywaydb.core.internal.util.StringUtils;

public class SerialItemMapper {

    public static SerialItem mapToEntity(SerialItemDto serialItemDto) {
        return new SerialItem(serialItemDto.getSerialNumber(), serialItemDto.getStatus(), serialItemDto.getPassportNumber(),
                serialItemDto.getNotes());
    }

    public static SerialItemDto mapToDto(SerialItem serialItem) {
        return new SerialItemDto(serialItem.getId(), serialItem.getSerialNumber(),
                serialItem.getStatus(), serialItem.getPassportNumber(), serialItem.getNotes());
    }

    public static SerialItem mapToUpdateEntity(SerialItem serialItem, SerialItemDto serialItemDto) {
        if (StringUtils.hasText(serialItemDto.getSerialNumber())) {
            serialItem.setSerialNumber(serialItemDto.getSerialNumber());
        }
        if (serialItemDto.getStatus() != null) {
            serialItem.setStatus(serialItemDto.getStatus());
        }
        if (StringUtils.hasText(serialItemDto.getPassportNumber())) {
            serialItem.setPassportNumber(serialItemDto.getPassportNumber());
        }
        if (StringUtils.hasText(serialItemDto.getNotes())) {
            serialItem.setNotes(serialItemDto.getNotes());
        }

        return serialItem;
    }
}
