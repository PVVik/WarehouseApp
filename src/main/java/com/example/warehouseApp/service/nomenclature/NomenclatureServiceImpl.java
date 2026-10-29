package com.example.warehouseApp.service.nomenclature;

import com.example.warehouseApp.domain.LogAudit;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.exception.AlreadyExistsException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.mapper.NomenclatureMapper;
import com.example.warehouseApp.model.Nomenclature;
import com.example.warehouseApp.repository.NomenclatureRepository;
import com.example.warehouseApp.specification.nomenclature.NomenclatureSpecificationsBuilder;
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
public class NomenclatureServiceImpl implements NomenclatureService {

    private final NomenclatureRepository nomenclatureRepository;

    @Override
    @Transactional
    @LogAudit(action = "CREATE", entityType = Nomenclature.class)
    public NomenclatureDto create(NomenclatureDto nomenclatureDto) {
        checkUniqueSku(nomenclatureDto.getSku());

        var nomenclature = nomenclatureRepository.save(NomenclatureMapper.mapToEntity(nomenclatureDto));

        log.info("Сохранили позицию под id {}", nomenclature.getId());

        return NomenclatureMapper.mapToDto(nomenclature);
    }

    @Override
    @Transactional
    @LogAudit(action = "UPDATE", entityType = Nomenclature.class)
    public NomenclatureDto update(NomenclatureDto nomenclatureDto) {
        var nomenclature = getEntityById(nomenclatureDto.getId());
        var updated = NomenclatureMapper.mapToUpdateEntity(nomenclature, nomenclatureDto);
        var saved = nomenclatureRepository.save(updated);

        log.info("Обновили позицию с id {}", saved.getId());

        return NomenclatureMapper.mapToDto(saved);
    }

    @Override
    public Page<NomenclatureDto> getAll(Pageable pageable, String search) {
        Page<Nomenclature> page;

        if (search == null || search.isBlank()) {
            page = nomenclatureRepository.findAll(pageable);
        } else {
            var builder = new NomenclatureSpecificationsBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var spec = builder.build();
            page = nomenclatureRepository.findAll(spec, pageable);
        }
        var nomenclatures = page.stream()
                .map(NomenclatureMapper::mapToDto)
                .sorted(NomenclatureDto::compareTo)
                .toList();

        log.info("Получили выгрузку позиций");

        return new PageImpl<>(nomenclatures, pageable, page.getTotalElements());
    }

    @Override
    @LogAudit(action = "DELETE", entityType = Nomenclature.class)
    public void delete(long id) {
        var nomenclature = getEntityById(id);

        log.info("Удалили позицию с id {}", nomenclature.getId());

        nomenclatureRepository.deleteById(nomenclature.getId());
    }

    @Override
    public NomenclatureDto getById(long id) {
        var nomenclature = getEntityById(id);

        log.info("Нашли позицию по id {}", nomenclature.getId());

        return NomenclatureMapper.mapToDto(nomenclature);
    }

    @Override
    public NomenclatureDto getBySku(String sku) {
        var nomenclature = nomenclatureRepository.findBySku(sku).orElseGet(() -> {
            log.warn("Не найдена позиция с артикулом: {}", sku);
            throw new NotFoundException(String.format("Не найдено позиции с артикулом %s", sku));
        });

        return NomenclatureMapper.mapToDto(nomenclature);
    }

    private void checkUniqueSku(String sku) {
        if (nomenclatureRepository.existsBySku(sku)) {
            log.warn("Проверка на уникальность sku провалилась");
            throw new AlreadyExistsException("Позиция с переданным артикулом уже существует");
        }
    }

    private Nomenclature getEntityById(long id) {
        return nomenclatureRepository.findById(id).orElseGet(() -> {
            log.warn("Не найдена позиция с id: {}", id);
            throw new NotFoundException(String.format("Не найдено позиции с id %d", id));
        });
    }

}
