package com.example.WarehouseApp.service.serialItem;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.dto.SerialItemDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.BatchMapper;
import com.example.WarehouseApp.mapper.NomenclatureMapper;
import com.example.WarehouseApp.mapper.SerialItemMapper;
import com.example.WarehouseApp.model.SerialItem;
import com.example.WarehouseApp.repository.SerialItemRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.specification.serialItem.SerialItemSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SerialItemService implements BaseService<SerialItemDto> {

    private final SerialItemRepository serialItemRepository;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<BatchDto> batchService;

    @Override
    @Transactional
    public SerialItemDto create(SerialItemDto serialItemDto) {
        var nomenclatureDto = nomenclatureService.getById(serialItemDto.getNomenclatureId());
        var batchDto = (serialItemDto.getBatchId() != null)
                ? batchService.getById(serialItemDto.getBatchId())
                : null;

        var serialItem = SerialItemMapper.mapToEntity(serialItemDto);
        serialItem.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclatureDto));
        if (batchDto != null) {
            serialItem.setBatch(BatchMapper.mapToEntityWithId(batchDto));
        }

        var saved = serialItemRepository.save(serialItem);

        log.info("Создали серийную единицу с id {}", serialItem.getId());

        var dtoSaved = SerialItemMapper.mapToDto(saved);
        setToDtoInfNomenclatureAndBatch(saved, dtoSaved);

        return dtoSaved;
    }

    @Override
    @Transactional
    public SerialItemDto update(SerialItemDto serialItemDto) {
        var saved = getEntityById(serialItemDto.getId());
        var newSerialItem = serialItemRepository.save(SerialItemMapper.mapToUpdateEntity(saved, serialItemDto));

        log.info("Обновили серийную единицу с id {}", newSerialItem.getId());

        var newDto = SerialItemMapper.mapToDto(newSerialItem);
        setToDtoInfNomenclatureAndBatch(newSerialItem, newDto);

        return newDto;
    }

    @Override
    public SerialItemDto getById(long id) {
        var serialItem = getEntityById(id);
        var dto = SerialItemMapper.mapToDto(serialItem);
        setToDtoInfNomenclatureAndBatch(serialItem, dto);

        log.info("Получили серийную единицу по id {}", serialItem.getId());

        return dto;
    }

    @Override
    public Page<SerialItemDto> getAll(Pageable pageable, String search) {
        Page<SerialItem> page;

        if (search == null || search.isBlank()) {
            page = serialItemRepository.findAll(pageable);
        } else {
            var builder = new SerialItemSpecificationBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var spec = builder.build();
            page = serialItemRepository.findAll(spec, pageable);
        }
        var dtos = page.stream()
                .map(SerialItemMapper::mapToDto)
                .sorted(SerialItemDto::compareTo)
                .toList();

        log.info("Получили выгрузку серийных единиц");

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public void delete(long id) {
        var serialItem = getEntityById(id);

        log.info("Удалили серийную единицу по id {}", serialItem.getId());

        serialItemRepository.deleteById(id);
    }

    private SerialItem getEntityById(long id) {
        return serialItemRepository.findById(id).orElseGet(() -> {
            log.warn("Не нашли серийную единицу с id {}", id);
            throw new NotFoundException(String.format("Серийная единица с id %d не найдена", id));
        });
    }

    private void setToDtoInfNomenclatureAndBatch(SerialItem serialItem, SerialItemDto serialItemDto) {
        serialItemDto.setNomenclatureId(serialItem.getNomenclature().getId());
        if (serialItem.getBatch() != null) {
            serialItemDto.setBatchId(serialItem.getBatch().getId());
        }
    }
}
