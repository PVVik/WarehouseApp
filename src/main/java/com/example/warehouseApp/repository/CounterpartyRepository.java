package com.example.warehouseApp.repository;

import com.example.warehouseApp.model.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long>, JpaSpecificationExecutor<Counterparty> {

    boolean existsByInn(String inn);

    Optional<Counterparty> findByInn(String inn);
}
