package com.example.warehouseApp.dto.movement;

import com.example.warehouseApp.model.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MovementDtoWriteOff {

    private Long id;

    private MovementType movementType;

    @NotNull(message = "Номенклатура обязательна к указанию")
    private Long nomenclatureId;

    private Long batchId;

    private Long serialItemId;

    @NotNull(message = "Количество обязательно к указанию")
    @Positive(message = "Количество не может быть меньше нуля")
    private BigDecimal quantity;

    @NotNull(message = "Склад списания обязателен к указанию")
    private Long warehouseFrom;

    @NotNull(message = "Ячейка для списания обязательна к указанию")
    private Long storageLocationId;

    @NotBlank(message = "Объяснение списания обязательно к указанию")
    private String notes;

    private String number;

    private LocalDate date;

    public MovementDtoWriteOff(Long id, MovementType movementType, Long nomenclatureId, BigDecimal quantity,
                               Long warehouseFrom, Long storageLocationId, String notes, String number,
                               LocalDate date) {
        this.id = id;
        this.movementType = movementType;
        this.nomenclatureId = nomenclatureId;
        this.quantity = quantity;
        this.warehouseFrom = warehouseFrom;
        this.storageLocationId = storageLocationId;
        this.notes = notes;
        this.number = number;
        this.date = date;
    }
}
