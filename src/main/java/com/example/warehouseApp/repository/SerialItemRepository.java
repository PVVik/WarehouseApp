package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.ItemStatus;
import com.example.warehouseApp.model.SerialItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SerialItemRepository extends JpaRepository<SerialItem, Long>, JpaSpecificationExecutor<SerialItem> {

    List<SerialItem> findAllByNomenclatureIdAndStatus(Long nomenclatureId, ItemStatus status);

}
