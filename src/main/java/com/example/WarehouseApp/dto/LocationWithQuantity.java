package com.example.WarehouseApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class LocationWithQuantity {

    private Long id;
    private String code;
    private BigDecimal quantity;

}
