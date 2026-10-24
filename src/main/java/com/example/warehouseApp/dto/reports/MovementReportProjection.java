package com.example.warehouseApp.dto.reports;

import com.example.warehouseApp.model.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MovementReportProjection {

    LocalDate getDate();

    MovementType getDocumentType();

    String getDocumentNumber();

    String getNomenclatureName();

    BigDecimal getQty();
}