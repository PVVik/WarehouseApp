package com.example.WarehouseApp.service.storageLocation;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.dto.WarehouseDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.StorageLocationMapper;
import com.example.WarehouseApp.model.StorageLocation;
import com.example.WarehouseApp.repository.StorageLocationRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.specification.storageLocation.StorageLocationSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageLocationServiceImpl implements StorageLocationService<StorageLocationDto> {

    private final StorageLocationRepository storageLocationRepository;
    private final BaseService<WarehouseDto> warehouseService;

    @Override
    @Transactional
    public StorageLocationDto create(StorageLocationDto storageLocationDto) {
        var storageLocation = storageLocationRepository.save(StorageLocationMapper.mapToEntity(storageLocationDto));
        var newLocation = StorageLocationMapper.mapToDto(storageLocation);
        setInfAboutWarehouse(newLocation);

        log.info("Создали ячейку с Id {}", newLocation.getId());

        return newLocation;
    }

    @Override
    @Transactional
    public StorageLocationDto update(StorageLocationDto storageLocationDto) {
        var savedLocation = getEntityById(storageLocationDto.getId());
        var updatedLocation = StorageLocationMapper.mapToUpdateEntity(savedLocation, storageLocationDto);
        var newSavedLocation = storageLocationRepository.save(updatedLocation);
        var newSavedLocationDto = StorageLocationMapper.mapToDto(newSavedLocation);
        setInfAboutWarehouse(newSavedLocationDto);

        log.info("Обновили ячейку с id {}", newSavedLocation.getId());

        return StorageLocationMapper.mapToDto(newSavedLocation);
    }

    @Override
    public StorageLocationDto getById(long id) {
        var storageLocationDto = StorageLocationMapper.mapToDto(getEntityById(id));
        setInfAboutWarehouse(storageLocationDto);

        log.info("Получили ячейку по id {}", storageLocationDto.getId());

        return storageLocationDto;
    }

    @Override
    public void delete(long id) {
        getEntityById(id);

        log.info("Удалили ячейку с id {}", id);

        storageLocationRepository.deleteById(id);
    }

    @Override
    public Page<StorageLocationDto> getAll(Pageable pageable, String search) {
        Page<StorageLocation> page;

        if (search == null || search.isBlank()) {
            page = storageLocationRepository.findAll(pageable);
        } else {
            var builder = new StorageLocationSpecificationBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var spec = builder.build();
            page = storageLocationRepository.findAll(spec, pageable);
        }
        var locations = page.stream()
                .map(StorageLocationMapper::mapToDto)
                .map(this::setInfAboutWarehouse)
                .sorted(StorageLocationDto::compareTo)
                .toList();

        log.info("Получили выгрузку ячеек");

        return new PageImpl<>(locations, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public BulkCreateResult createBulk(Long warehouseId, String zone, String rack, int count) {
        var normalizedZone = (zone == null) ? "" : zone.trim().toUpperCase();
        if (normalizedZone.isBlank() || !normalizedZone.matches("[A-ZА-Я0-9]{1,2}")) {
            return new BulkCreateResult(false, 0, 0,
                    "Зона должна быть 1-2 символами (буквы или цифры), например: A или B2");
        }
        if (rack == null || rack.isBlank() || !rack.matches("\\d+")) {
            return new BulkCreateResult(false, 0, 0,
                    "Стеллаж должен быть числом, например: 1");
        }
        if (count < 1 || count > 100) {
            return new BulkCreateResult(false, 0, 0,
                    "Количество ячеек должно быть от 1 до 100");
        }

        var existingCodes = getAll(Pageable.unpaged(), "warehouseId:" + warehouseId + ",").stream()
                .map(StorageLocationDto::getCode)
                .collect(Collectors.toSet());

        int created = 0;
        int skipped = 0;

        for (int shelf = 1; shelf <= count; shelf++) {
            var code = normalizedZone + "-" + rack + "-" + shelf;
            if (existingCodes.contains(code)) {
                skipped++;
                continue;
            }

            var dto = new StorageLocationDto();
            dto.setWarehouseId(warehouseId);
            dto.setZone(normalizedZone);
            dto.setRack(rack);
            dto.setShelf(String.valueOf(shelf));
            dto.setCode(code);
            dto.setUsed(false);

            create(dto);
            existingCodes.add(code);
            created++;
        }

        var message = "Создано ячеек: " + created +
                (skipped > 0 ? ", пропущено (уже существуют): " + skipped : "");
        return new BulkCreateResult(true, created, skipped, message);
    }

    private StorageLocation getEntityById(long id) {
        return storageLocationRepository.findById(id).orElseGet(() -> {
            log.warn("Не нашли ячейку с id {}", id);
            throw new NotFoundException(String.format("Ячейка с id %d не найдена", id));
        });
    }

    private StorageLocationDto setInfAboutWarehouse(StorageLocationDto storageLocationDto) {
        var warehouse = warehouseService.getById(storageLocationDto.getWarehouseId());
        storageLocationDto.setWarehouseName(warehouse.getName());

        log.info("Добавили информацию о складе к ячейке {}: id {}, name {}", storageLocationDto.getId(),
                storageLocationDto.getWarehouseId(), storageLocationDto.getWarehouseName());

        return storageLocationDto;
    }

}
