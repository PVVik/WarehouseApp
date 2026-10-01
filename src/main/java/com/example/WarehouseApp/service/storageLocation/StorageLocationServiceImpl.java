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

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageLocationServiceImpl implements BaseService<StorageLocationDto> {

    private final StorageLocationRepository storageLocationRepository;
    private final BaseService<WarehouseDto> warehouseService;

    @Override
    public StorageLocationDto create(StorageLocationDto storageLocationDto) {
        var storageLocation = storageLocationRepository.save(StorageLocationMapper.mapToEntity(storageLocationDto));
        var newLocation = StorageLocationMapper.mapToDto(storageLocation);
        setInfAboutWarehouse(newLocation);

        log.info("Создали ячейку с Id {}", newLocation.getId());

        return newLocation;
    }

    @Override
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
