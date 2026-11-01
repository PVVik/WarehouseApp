package com.example.warehouseApp.dto.movement;

import com.example.warehouseApp.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class MovementDtoTransfer {

    private long id;

    @NotNull(message = "Тип движения обязателен к указанию")
    private MovementType movementType;

    @NotNull(message = "Номенклатура обязательна к указанию")
    private Long nomenclatureId;

    private Long batchId;

    private Long serialItemId;

    @NotNull(message = "Склад-отправитель обязателен к указанию")
    private Long warehouseFrom;

    @NotNull(message = "Склад-получатель обязателен к указанию")
    private Long warehouseTo;

    @NotNull(message = "Выберите ячейку для отгрузки")
    private Long storageLocationId;

    @NotNull(message = "Количество обязательно к указанию")
    @Positive(message = "Количество не может быть меньше нуля")
    private BigDecimal quantity;

    private String number;

    private LocalDate date;

    private String notes;

    public MovementDtoTransfer(long id, MovementType movementType, Long nomenclatureId, Long warehouseFrom,
                               Long warehouseTo, BigDecimal quantity, String number, LocalDate date, String notes) {
        this.id = id;
        this.movementType = movementType;
        this.nomenclatureId = nomenclatureId;
        this.warehouseFrom = warehouseFrom;
        this.warehouseTo = warehouseTo;
        this.quantity = quantity;
        this.number = number;
        this.date = date;
        this.notes = notes;
    }
}
