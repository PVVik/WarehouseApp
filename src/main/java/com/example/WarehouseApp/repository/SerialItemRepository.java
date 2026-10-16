package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.ItemStatus;
import com.example.WarehouseApp.model.SerialItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SerialItemRepository extends JpaRepository<SerialItem, Long>, JpaSpecificationExecutor<SerialItem> {

    List<SerialItem> findAllByNomenclatureIdAndStatus(Long nomenclatureId, ItemStatus status);

}
