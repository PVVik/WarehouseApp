package com.example.WarehouseApp.dto.movement;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementDtoReceipt {

    private long id;

    @NotNull(message = "Тип документа обязателен к заполнению")
    private MovementType movementType;

    @NotNull(message = "Тип номенклатуры обязателен к заполнению")
    private Long nomenclatureId;

    private BatchDto batchDto;

    private SerialItemDto serialItemDto;

    @NotNull(message = "Склад обязателен к заполнению")
    private Long warehouseTo;

    private Long storageLocationId;

    @NotNull(message = "Поставщик обязателен к заполнению")
    private Long counterpartyId;

    @NotNull(message = "Количество обязательно к заполнению")
    @Positive(message = "Количество не может быть отрицательным")
    private BigDecimal quantity;

    @NotNull(message = "Цена обязательна к заполнению")
    @Positive(message = "Цена не может быть отрицательной")
    private BigDecimal price;

    private String number;

    private LocalDate date;

    @NotNull(message = "id сотрудника обязателен к заполнению")
    private Long userId;

    private String notes;

    public MovementDtoReceipt(long id, MovementType movementType, Long nomenclatureId, Long warehouseTo,
                              Long counterpartyId, BigDecimal quantity, BigDecimal price,
                              String number, LocalDate date, Long userId, String notes) {
        this.id = id;
        this.movementType = movementType;
        this.nomenclatureId = nomenclatureId;
        this.warehouseTo = warehouseTo;
        this.counterpartyId = counterpartyId;
        this.quantity = quantity;
        this.price = price;
        this.number = number;
        this.date = date;
        this.userId = userId;
        this.notes = notes;
    }

}
