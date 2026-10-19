package com.example.warehouseApp.service.counterparty;

import com.example.warehouseApp.dto.CounterpartyDto;
import com.example.warehouseApp.exception.AlreadyExistsException;
import com.example.warehouseApp.exception.NotFoundException;
import com.example.warehouseApp.mapper.CounterpartyMapper;
import com.example.warehouseApp.model.Counterparty;
import com.example.warehouseApp.repository.CounterpartyRepository;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.specification.counterparty.CounterpartySpecificationBuilder;
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
public class CounterpartyServiceImpl implements BaseService<CounterpartyDto> {

    private final CounterpartyRepository counterpartyRepository;

    @Override
    @Transactional
    public CounterpartyDto create(CounterpartyDto counterpartyDto) {
        checkUniqueInn(counterpartyDto.getInn());

        var counterparty = counterpartyRepository.save(CounterpartyMapper.mapToEntity(counterpartyDto));

        log.info("Добавили контрагента с id {}", counterparty.getId());

        return CounterpartyMapper.mapToDto(counterparty);
    }

    @Override
    public CounterpartyDto getById(long id) {
        var counterparty = getEntityById(id);

        log.info("Нашли контрагента с id {}", counterparty.getId());

        return CounterpartyMapper.mapToDto(counterparty);
    }

    @Override
    @Transactional
    public CounterpartyDto update(CounterpartyDto counterpartyDto) {
        var counterparty = getEntityById(counterpartyDto.getId());
        var updated = CounterpartyMapper.mapToUpdateEntity(counterparty, counterpartyDto);
        var savedUpdated = counterpartyRepository.save(updated);

        log.info("Обновили контрагента с id {}", savedUpdated.getId());

        return CounterpartyMapper.mapToDto(savedUpdated);
    }

    @Override
    public Page<CounterpartyDto> getAll(Pageable pageable, String search) {
        Page<Counterparty> page;
        if (search == null || search.isBlank()) {
            page = counterpartyRepository.findAll(pageable);
        } else {
            var counterpartySpecificationBuilder = new CounterpartySpecificationBuilder();
            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                counterpartySpecificationBuilder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }

            var specification = counterpartySpecificationBuilder.build();
            page = counterpartyRepository.findAll(specification, pageable);
        }

        var dtos = page.stream()
                .map(CounterpartyMapper::mapToDto)
                .sorted(CounterpartyDto::compareTo)
                .toList();

        log.info("Получили выгрузку контрагентов");

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public void delete(long id) {
        var counterparty = getEntityById(id);

        log.info("Удалили контрагента с id {}", counterparty.getId());

        counterpartyRepository.deleteById(counterparty.getId());
    }

    private void checkUniqueInn(String inn) {
        if (counterpartyRepository.existsByInn(inn)) {
            log.warn("Проверка на уникальность ИНН {} провалилась", inn);
            throw new AlreadyExistsException(String.format("Контрагент с ИНН %s уж существует", inn));
        }
    }

    private Counterparty getEntityById(long id) {
        return counterpartyRepository.findById(id).orElseGet(() -> {
            log.warn("Контрагент с id {} не был найден", id);
            throw new NotFoundException(String.format("Контрагента с id %s не существует", id));
        });
    }
}
