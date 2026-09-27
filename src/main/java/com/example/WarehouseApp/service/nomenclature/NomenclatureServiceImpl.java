package com.example.WarehouseApp.service.nomenclature;

import com.example.WarehouseApp.dto.NomenclatureDto;
import com.example.WarehouseApp.exception.NotFoundException;
import com.example.WarehouseApp.mapper.NomenclatureMapper;
import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.repository.NomenclatureRepository;
import com.example.WarehouseApp.specification.NomenclatureSpecificationsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class NomenclatureServiceImpl implements NomenclatureService {

    private final NomenclatureRepository nomenclatureRepository;

    @Override
    public NomenclatureDto addNomenclature(NomenclatureDto nomenclatureDto) {
        var nomenclature = nomenclatureRepository.save(NomenclatureMapper.mapToEntity(nomenclatureDto));

        log.info("Сохранили позицию под id {}", nomenclature.getId());

        return NomenclatureMapper.mapToDto(nomenclature);
    }

    @Override
    public NomenclatureDto updateNomenclature(NomenclatureDto nomenclatureDto) {
        var nomenclature = nomenclatureRepository.findById(nomenclatureDto.getId()).orElseThrow(() ->
                new NotFoundException(String.format("Позиция с id %d не найдена", nomenclatureDto.getId())));
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
    public NomenclatureDto getById(long id) {
        var nomenclature = nomenclatureRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Позиция с id %d не найдена", id)));

        log.info("Нашли позицию по id {}", nomenclature.getId());

        return NomenclatureMapper.mapToDto(nomenclature);
    }

    @Override
    public NomenclatureDto getBySku(String sku) {
        var nomenclature = nomenclatureRepository.findBySku(sku).orElseThrow(() ->
                new NotFoundException(String.format("Не найдено позиции с артикулом %s", sku)));

        log.info("Нашли позицию по sku {}", sku);

        return NomenclatureMapper.mapToDto(nomenclature);
    }

}
