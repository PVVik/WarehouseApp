package com.example.warehouseApp.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineInputItem {

    private Long id;

    @DecimalMin(value = "0.0", message = "Количество не может быть отрицательным")
    private BigDecimal actualQty;
}