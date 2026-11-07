package com.example.warehouseApp.service.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.counterparty.CounterpartyCsvRow;
import com.example.warehouseApp.service.BaseService;

import java.util.List;

public interface CounterpartyService extends BaseService<CounterpartyDto> {

    ImportResult importAll(List<CounterpartyCsvRow> rows);
}
