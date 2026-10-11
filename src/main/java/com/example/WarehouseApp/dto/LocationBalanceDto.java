package com.example.WarehouseApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LocationBalanceDto {

    private Long id;
    private String code;
    private BigDecimal balance;

}
