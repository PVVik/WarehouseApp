package com.example.warehouseApp.service.warehouse;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.WarehouseDto;
import com.example.warehouseApp.exception.BusinessException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.mapper.WarehouseMapper;
import com.example.warehouseApp.model.Warehouse;
import com.example.warehouseApp.parser.ImportResult;
import com.example.warehouseApp.parser.warehouse.WarehouseCsvRow;
import com.example.warehouseApp.repository.WarehouseRepository;
import com.example.warehouseApp.specification.warehouse.WarehouseSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    @LogAudit(action = "CREATE", entityType = Warehouse.class)
    public WarehouseDto create(WarehouseDto warehouseDto) {
        if (warehouseRepository.findByName(warehouseDto.getName()).isPresent()) {
            throw new BusinessException(String.format("Склад с названием %s уже существует", warehouseDto.getName()));
        }
        var warehouse = warehouseRepository.save(WarehouseMapper.mapToEntity(warehouseDto));
        log.info("Создали склад с id {}", warehouse.getId());

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Override
    @Transactional
    @LogAudit(action = "UPDATE", entityType = Warehouse.class)
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
    @LogAudit(action = "DELETE", entityType = Warehouse.class)
    public void delete(long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Склад с id %d не найден", id)));

        log.info("Удалили склад с id {}", warehouse.getId());

        warehouseRepository.deleteById(warehouse.getId());
    }

    @Override
    public WarehouseDto getByName(String name) {
        var warehouse = warehouseRepository.findByName(name).orElseGet(() -> {
            log.warn("Не найдено склада по названию {}", name);
            throw new NotFoundException(String.format("Склад с названием %s не найден", name));
        });
        log.info("Вернули склад по названию {}", name);

        return WarehouseMapper.mapToDto(warehouse);
    }

    @Transactional
    @Override
    @LogAudit(action = "CREATE", entityType = Warehouse.class)
    public ImportResult importAll(List<WarehouseCsvRow> rows) {
        int created = 0;
        int updated = 0;
        LocalDateTime now = LocalDateTime.now();

        for (WarehouseCsvRow row : rows) {
            Optional<Warehouse> existing = warehouseRepository.findByName(row.name());

            if (existing.isPresent()) {
                Warehouse w = existing.get();
                w.setAddress(row.address());
                w.setType(row.type());
                w.setActive(true);
                w.setUpdatedAt(now);
                updated++;
                log.info("Обновили существующий склад с id {}", w.getId());
            } else {
                Warehouse w = new Warehouse(row.name(), row.address(), row.type(),
                        true, now, now);
                warehouseRepository.save(w);
                created++;
                log.info("Создали склад с названием {}", w.getName());
            }
        }
        return new ImportResult(created, updated);
    }
}
