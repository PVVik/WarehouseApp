package com.example.warehouseApp.service.serialItem;

import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.SerialItemDto;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.mapper.BatchMapper;
import com.example.warehouseApp.mapper.NomenclatureMapper;
import com.example.warehouseApp.mapper.SerialItemMapper;
import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.SerialItem;
import com.example.warehouseApp.repository.SerialItemRepository;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.batch.BatchService;
import com.example.warehouseApp.service.stockBalance.StockBalanceService;
import com.example.warehouseApp.specification.serialItem.SerialItemSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SerialItemServiceImpl implements SerialItemService {

    private final SerialItemRepository serialItemRepository;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BatchService batchService;
    private final StockBalanceService stockBalanceService;

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

    @Override
    public List<SerialItemDto> findByNomenclatureAndWarehouseWithStock(
            Long nomenclatureId, Long warehouseId, ItemStatus status) {

        if (nomenclatureId == null || warehouseId == null) {
            return List.of();
        }

        var candidates = serialItemRepository.findAllByNomenclatureIdAndStatus(nomenclatureId, status);
        if (candidates.isEmpty()) {
            return List.of();
        }

        var balances = stockBalanceService.findPositiveBalancesByWarehouse(warehouseId);

        var inStockIds = balances.stream()
                .filter(sb -> sb.getSerialItem() != null)
                .map(sb -> sb.getSerialItem().getId())
                .collect(Collectors.toSet());

        return candidates.stream()
                .filter(si -> inStockIds.contains(si.getId()))
                .map(SerialItemMapper::mapToDto)
                .toList();
    }

    @Override
    public Optional<SerialItem> findBySerialNumber(String serialNumber) {
        return serialItemRepository.findBySerialNumber(serialNumber);
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
