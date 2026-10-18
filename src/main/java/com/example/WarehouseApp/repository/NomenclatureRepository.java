package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.Nomenclature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface NomenclatureRepository extends JpaRepository<Nomenclature, Long>, JpaSpecificationExecutor<Nomenclature> {

    Optional<Nomenclature> findBySku(String sku);

    boolean existsBySku(String sku);
}
