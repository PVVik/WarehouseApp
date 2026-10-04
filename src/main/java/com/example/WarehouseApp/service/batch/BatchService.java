package com.example.WarehouseApp.service.batch;

import com.example.WarehouseApp.dto.BatchDto;
import com.example.WarehouseApp.dto.CounterpartyDto;
import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.BatchMapper;
import com.example.WarehouseApp.mapper.CounterpartyMapper;
import com.example.WarehouseApp.mapper.NomenclatureMapper;
import com.example.WarehouseApp.model.Batch;
import com.example.WarehouseApp.repository.BatchRepository;
import com.example.WarehouseApp.service.BaseService;
import com.example.WarehouseApp.specification.batch.BatchSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchService implements BaseService<BatchDto> {

    private final BatchRepository batchRepository;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;

    @Override
    @Transactional
    public BatchDto create(BatchDto batchDto) {
        var nomenclatureDto = nomenclatureService.getById(batchDto.getNomenclatureId());
        var supplierDto = (batchDto.getSupplierId() != null)
                ? counterpartyService.getById(batchDto.getSupplierId())
                : null;

        var newBatch = BatchMapper.mapToEntity(batchDto);
        newBatch.setNomenclature(NomenclatureMapper.mapToEntityWithId(nomenclatureDto));
        if (supplierDto != null) {
            newBatch.setSupplier(CounterpartyMapper.mapToEntityWithId(supplierDto));
        }
        var saved = batchRepository.save(newBatch);

        log.info("Создали партию с id {}", newBatch.getId());

        var savedDto = BatchMapper.mapToDto(saved);
        setInDtoInfNomenclatureAndSuppler(saved, savedDto);

        return savedDto;
    }

    @Override
    @Transactional
    public BatchDto update(BatchDto batchDto) {
        var saved = getEntityById(batchDto.getId());
        var newBatch = batchRepository.save(BatchMapper.mapToUpdateEntity(saved, batchDto));

        log.info("Обновили партию с id {}", newBatch.getId());

        var newDto = BatchMapper.mapToDto(newBatch);
        setInDtoInfNomenclatureAndSuppler(newBatch, newDto);

        return newDto;
    }

    @Override
    public BatchDto getById(long id) {
        var batch = getEntityById(id);
        var batchDto = BatchMapper.mapToDto(batch);
        setInDtoInfNomenclatureAndSuppler(batch, batchDto);

        log.info("Получили партию по id {}", batch.getId());

        return batchDto;
    }

    @Override
    public Page<BatchDto> getAll(Pageable pageable, String search) {
        Page<Batch> page;
        if (search == null || search.isBlank()) {
            page = batchRepository.findAll(pageable);
        } else {
            var batchSpecificationBuilder = new BatchSpecificationBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                batchSpecificationBuilder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var specification = batchSpecificationBuilder.build();
            page = batchRepository.findAll(specification, pageable);
        }

        var dtos = page.stream()
                .map(BatchMapper::mapToDto)
                .sorted(BatchDto::compareTo)
                .toList();

        log.info("Получили выгрузку партий");

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public void delete(long id) {
        var batch = getEntityById(id);

        log.info("Удалили партию по id {}", batch.getId());

        batchRepository.deleteById(batch.getId());
    }

    private Batch getEntityById(long id) {
        return batchRepository.findById(id).orElseGet(() -> {
            log.warn("Не нашли партию по id {}", id);
            throw new NotFoundException(String.format("Партия с id %d не найдена", id));
        });
    }

    private void setInDtoInfNomenclatureAndSuppler(Batch batch, BatchDto batchDto) {
        batchDto.setNomenclatureId(batch.getNomenclature().getId());
        if (batch.getSupplier() != null) {
            batchDto.setSupplierId(batch.getSupplier().getId());
        }
    }
}
