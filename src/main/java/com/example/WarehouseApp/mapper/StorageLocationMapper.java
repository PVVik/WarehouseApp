package com.example.WarehouseApp.mapper;

import com.example.WarehouseApp.dto.StorageLocationDto;
import com.example.WarehouseApp.model.StorageLocation;
import org.flywaydb.core.internal.util.StringUtils;

public class StorageLocationMapper {

    public static StorageLocation mapToEntity(StorageLocationDto storageLocationDto) {
        return new StorageLocation(storageLocationDto.getWarehouseId(), storageLocationDto.getZone(),
                storageLocationDto.getRack(), storageLocationDto.getShelf(), storageLocationDto.getUsed());
    }

    public static StorageLocation mapToEntityWithId(StorageLocationDto storageLocationDto) {
        return new StorageLocation(storageLocationDto.getId(), storageLocationDto.getWarehouseId(), storageLocationDto.getZone(),
                storageLocationDto.getRack(), storageLocationDto.getShelf(), storageLocationDto.getUsed());
    }

    public static StorageLocationDto mapToDto(StorageLocation storageLocation) {
        return new StorageLocationDto(storageLocation.getId(), storageLocation.getWarehouseId(), storageLocation.getZone(),
                storageLocation.getRack(), storageLocation.getShelf(), storageLocation.getCode(), storageLocation.isUsed());
    }

    public static StorageLocation mapToUpdateEntity(StorageLocation storageLocation,
                                                    StorageLocationDto storageLocationDto) {
        if (StringUtils.hasText(storageLocationDto.getZone())) {
            storageLocation.setZone(storageLocationDto.getZone());
        }
        if (StringUtils.hasText(storageLocationDto.getRack())) {
            storageLocation.setRack(storageLocationDto.getRack());
        }
        if (StringUtils.hasText(storageLocationDto.getShelf())) {
            storageLocation.setShelf(storageLocationDto.getShelf());
        }
        if (storageLocationDto.getUsed() != null) {
            storageLocation.setUsed(storageLocationDto.getUsed());
        }
        storageLocation.setCode(storageLocation.createCode());

        return storageLocation;
    }
}
