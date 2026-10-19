package com.example.warehouseApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class LocationWithQuantity {

    private Long id;
    private String code;
    private BigDecimal quantity;

}
