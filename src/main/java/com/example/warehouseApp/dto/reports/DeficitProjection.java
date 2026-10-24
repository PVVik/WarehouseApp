package com.example.warehouseApp.dto.reports;

import java.math.BigDecimal;

public interface DeficitProjection {

    String getNomenclatureName();

    BigDecimal getCurrentStock();

    BigDecimal getMinThreshold();
}
