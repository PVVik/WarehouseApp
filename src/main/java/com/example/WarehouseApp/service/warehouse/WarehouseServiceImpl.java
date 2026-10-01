package com.example.WarehouseApp.service.warehouse;

import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.WarehouseMapper;
import com.example.WarehouseApp.model.Warehouse;
import com.example.WarehouseApp.repository.WarehouseRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.specification.warehouse.WarehouseSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements BaseService<WarehouseDto> {

    private final WarehouseRepository warehouseRepository;

    @Override
    public WarehouseDto create(WarehouseDto warehouseDto) {
        var warehouse = warehouseRepository.save(WarehouseMapper.mapToEntity(warehouseDto));
        log.info("Создали склад с id {}", warehouse.getId());

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Override
    public WarehouseDto update(WarehouseDto warehouseDto) {
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
    public WarehouseDto getById(long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", id)));

        log.info("Получили склад по id {}", id);

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Override
    public Page<WarehouseDto> getAll(Pageable pageable, String search) {
        Page<Warehouse> page;

        if (search == null || search.isBlank()) {
            page = warehouseRepository.findAll(pageable);
        } else {
            var builder = new WarehouseSpecificationBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var spec = builder.build();
            page = warehouseRepository.findAll(spec, pageable);
        }
        var warehouses = page.stream()
                .map(WarehouseMapper::mapToDto)
                .sorted(WarehouseDto::compareTo)
                .toList();

        log.info("Получили выгрузку складов");

        return new PageImpl<>(warehouses, pageable, page.getTotalElements());
    }

    @Override
    public void delete(long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", id)));

        log.info("Удалили склад с id {}", warehouse.getId());

        warehouseRepository.deleteById(warehouse.getId());
    }
}
