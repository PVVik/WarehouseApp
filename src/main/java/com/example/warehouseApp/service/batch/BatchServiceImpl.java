package com.example.warehouseApp.service.batch;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.BatchWithStockDto;
import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.exception.ValidationException;
import com.example.warehouseApp.mapper.BatchMapper;
import com.example.warehouseApp.mapper.CounterpartyMapper;
import com.example.warehouseApp.mapper.NomenclatureMapper;
import com.example.warehouseApp.model.Batch;
import com.example.warehouseApp.repository.BatchRepository;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.specification.batch.BatchSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BaseService<NomenclatureDto> nomenclatureService;
    private final BaseService<CounterpartyDto> counterpartyService;

    @Override
    @Transactional
    @LogAudit(action = "CREATE", entityType = Batch.class)
    public BatchDto create(BatchDto batchDto) {
        validDate(batchDto);
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

        return BatchMapper.mapToDto(saved);
    }

    @Override
    @Transactional
    @LogAudit(action = "UPDATE", entityType = Batch.class)
    public BatchDto update(BatchDto batchDto) {
        var saved = getEntityById(batchDto.getId());
        var newBatch = batchRepository.save(BatchMapper.mapToUpdateEntity(saved, batchDto));

        log.info("Обновили партию с id {}", newBatch.getId());

        return BatchMapper.mapToDto(newBatch);
    }

    @Override
    public BatchDto getById(long id) {
        var batch = getEntityById(id);
        var batchDto = BatchMapper.mapToDto(batch);

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
    @LogAudit(action = "DELETE", entityType = Batch.class)
    public void delete(long id) {
        var batch = getEntityById(id);

        log.info("Удалили партию по id {}", batch.getId());

        batchRepository.deleteById(batch.getId());
    }

    @Override
    public List<BatchWithStockDto> getBatchesWithStockOnWarehouse(Long nomenclatureId, Long warehouseId) {
        return batchRepository.findBatchesWithStockOnWarehouse(nomenclatureId, warehouseId);
    }

    @Override
    public Optional<Batch> findByBatchNumberAndNomenclatureId(String batchNumber, Long nomenclatureId) {
        return batchRepository.findByBatchNumberAndNomenclatureId(batchNumber, nomenclatureId);
    }

    private Batch getEntityById(long id) {
        return batchRepository.findById(id).orElseGet(() -> {
            log.warn("Не нашли партию по id {}", id);
            throw new NotFoundException(String.format("Партия с id %d не найдена", id));
        });
    }


    private void validDate(BatchDto batchDto) {
        if (batchDto.getProductionDate() != null && batchDto.getProductionDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата производства не может быть в будущем");
        }
        if (batchDto.getExpiryDate() != null && batchDto.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Срок годности не может быть в прошлом");
        }
        if (batchDto.getProductionDate() != null && batchDto.getExpiryDate() != null
                && !batchDto.getExpiryDate().isAfter(batchDto.getProductionDate())) {
            throw new ValidationException("Срок годности должен быть позже даты производства");
        }
    }
}
