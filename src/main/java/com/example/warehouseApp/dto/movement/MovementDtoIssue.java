package com.example.warehouseApp.dto.movement;

import com.example.warehouseApp.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MovementDtoIssue {

    private long id;

    @NotNull(message = "Тип документа обязателен к заполнению")
    private MovementType movementType;

    @NotNull(message = "Тип номенклатуры обязателен к заполнению")
    private Long nomenclatureId;

    private Long batchId;

    private Long serialItemId;

    @NotNull(message = "Склад обязателен к заполнению")
    private Long warehouseFrom;

    private Long storageLocationId;

    @NotNull(message = "Получатель обязателен к заполнению")
    private Long customerId;

    @NotNull(message = "Количество обязательно к заполнению")
    @Positive(message = "Количество не может быть меньше нуля")
    private BigDecimal quantity;

    @NotNull(message = "Цена обязательна к заполнению")
    @Positive(message = "Цена не может быть меньше нуля")
    private BigDecimal price;

    private String number;

    private LocalDate date;

    private String notes;

    public MovementDtoIssue(long id, MovementType movementType, Long nomenclatureId, Long warehouseFrom, Long customerId,
                            BigDecimal quantity, BigDecimal price, String number, LocalDate date, String notes) {
        this.id = id;
        this.movementType = movementType;
        this.nomenclatureId = nomenclatureId;
        this.warehouseFrom = warehouseFrom;
        this.customerId = customerId;
        this.quantity = quantity;
        this.price = price;
        this.number = number;
        this.date = date;
        this.notes = notes;
    }
}
