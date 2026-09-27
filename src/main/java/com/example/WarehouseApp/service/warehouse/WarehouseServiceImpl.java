package com.example.WarehouseApp.service.warehouse;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.WarehouseMapper;
import com.example.WarehouseApp.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseDto addWarehouse(WarehouseDto warehouseDto) {
        var warehouse = warehouseRepository.save(WarehouseMapper.mapToEntity(warehouseDto));
        log.info("Создали склад с id {}", warehouse.getId());

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Override
    public WarehouseDto updateWarehouse(WarehouseDto warehouseDto) {
        var warehouse = warehouseRepository.findById(warehouseDto.getId()).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", warehouseDto.getId())));
        log.info(warehouse.toString());
        var updatedWarehouse = WarehouseMapper.mapToUpdateEntity(warehouse, warehouseDto);
        log.info(updatedWarehouse.toString());
        warehouseRepository.save(updatedWarehouse);
        log.info("Успешно обновили склад с id {}", updatedWarehouse.getId());

        return WarehouseMapper.mapToDto(updatedWarehouse);
    }

    @Override
    public WarehouseDto getWarehouseById(long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", id)));

        log.info("Получили склад по id {}", id);

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Override
    public List<WarehouseDto> getWarehouses() {
        log.info("Получили список всех складов");

        return warehouseRepository.findAll().stream()
                .map(WarehouseMapper::mapToDto)
                .toList();
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouse(long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", id)));

        log.info("Удалили склад с id {}", warehouse.getId());

        warehouseRepository.deleteById(warehouse.getId());
    }
}
