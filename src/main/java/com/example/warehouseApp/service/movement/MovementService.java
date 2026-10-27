package com.example.warehouseApp.service.movement;

import com.example.warehouseApp.dto.movement.MovementResponseDto;
import com.example.warehouseApp.mapper.MovementMapper;
import com.example.warehouseApp.model.Movement;
import com.example.warehouseApp.repository.MovementRepository;
import com.example.warehouseApp.specification.container.SearchOperation;
import com.example.warehouseApp.specification.movement.MovementSpecificationBuilder;
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
public class MovementService {

    private final MovementRepository movementRepository;

    public Page<MovementResponseDto> getAll(Pageable pageable, String search) {
        Page<Movement> page;

        if (search == null || search.isBlank()) {
            page = movementRepository.findAll(pageable);
        } else {
            var builder = new MovementSpecificationBuilder();

            var pattern = Pattern.compile("(\\w+?)([:<>])(\\w+?),", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                String key = matcher.group(1);
                String opStr = matcher.group(2);
                String valStr = matcher.group(3);

                SearchOperation op = SearchOperation.getSimpleOperation(opStr.charAt(0));

                if (op != null) {
                    builder.with(key, opStr, valStr);
                }
            }

            var spec = builder.build();
            if (spec != null) {
                page = movementRepository.findAll(spec, pageable);
            } else {
                page = movementRepository.findAll(pageable);
            }
        }

        var dtoList = page.getContent().stream()
                .map(MovementMapper::mapToDto)
                .toList();

        log.info("Получили выгрузку движений. Элементы: {}, всего: {}", dtoList.size(), page.getTotalElements());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

}
