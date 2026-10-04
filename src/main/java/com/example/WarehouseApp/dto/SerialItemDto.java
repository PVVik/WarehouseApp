package com.example.WarehouseApp.dto;

import com.example.WarehouseApp.model.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SerialItemDto implements Comparable<SerialItemDto> {

    private long id;

    @NotNull(message = "Id позиции обязателен")
    private Long nomenclatureId;

    @NotBlank(message = "Серийный номер обязателен")
    private String serialNumber;

    private Long batchId;

    @NotNull(message = "Статус обязателен")
    private ItemStatus status;

    @NotBlank(message = "Номер паспорта обязателен")
    private String passportNumber;

    private String notes;

    public SerialItemDto(long id, String serialNumber, ItemStatus status, String passportNumber, String notes) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.status = status;
        this.passportNumber = passportNumber;
        this.notes = notes;
    }

    @Override
    public int compareTo(SerialItemDto o) {
        return Math.toIntExact(this.getId() - o.getId());
    }
}
