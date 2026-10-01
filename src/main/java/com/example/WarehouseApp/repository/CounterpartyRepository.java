package com.example.WarehouseApp.repository;

import com.example.WarehouseApp.model.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long>, JpaSpecificationExecutor<Counterparty> {

    boolean existsByInn(String inn);
}
