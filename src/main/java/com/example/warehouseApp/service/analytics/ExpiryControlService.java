package com.example.warehouseApp.service.analytics;

import com.example.warehouseApp.dto.BatchDto;
import com.example.warehouseApp.dto.NomenclatureDto;
import com.example.warehouseApp.dto.analytics.ExpiryControlItemDto;
import com.example.warehouseApp.service.BaseService;
import com.example.warehouseApp.service.batch.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryControlService {

    private final BatchService batchService;
    private final BaseService<NomenclatureDto> nomenclatureService;

    @Transactional(readOnly = true)
    public Page<ExpiryControlItemDto> getControlList(Pageable pageable, String search) {
        Page<BatchDto> page = batchService.getAll(pageable, search);
        List<BatchDto> batches = page.getContent();

        if (batches.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Set<Long> nomenclatureIds = batches.stream()
                .map(BatchDto::getNomenclatureId)
                .collect(Collectors.toSet());

        Map<Long, String> idToNameMap = new HashMap<>();
        for (Long id : nomenclatureIds) {
            var nomenDto = nomenclatureService.getById(id);
            idToNameMap.put(id, nomenDto.getName());
        }

        List<ExpiryControlItemDto> resultList = batches.stream().map(batch -> {
            Long nomenId = batch.getNomenclatureId();
            String name = idToNameMap.get(nomenId);

            ExpiryControlItemDto dto = new ExpiryControlItemDto();
            dto.setBatchId(batch.getId());
            dto.setBatchNumber(batch.getBatchNumber());
            dto.setNomenclatureName(name);
            dto.setExpiryDate(batch.getExpiryDate());
            dto.setStatus(batch.getStatus());

            return dto;
        }).toList();

        log.info("Получили информацию о состоянии партий");

        return new PageImpl<>(resultList, pageable, page.getTotalElements());
    }
}

