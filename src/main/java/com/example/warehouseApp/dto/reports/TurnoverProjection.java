package com.example.warehouseApp.dto.reports;

import java.math.BigDecimal;

public interface TurnoverProjection {

    Long getNomenclatureId();

    String getNomenclatureName();

    BigDecimal getTotalQuantity();

    BigDecimal getAvgMonthlyQuantity();

    BigDecimal getTotalAmount();
}