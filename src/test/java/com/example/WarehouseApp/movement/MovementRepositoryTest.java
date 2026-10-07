package com.example.WarehouseApp.movement;

import com.example.WarehouseApp.model.Movement;
import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.model.Warehouse;
import com.example.WarehouseApp.model.Counterparty;
import com.example.WarehouseApp.model.Batch;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.model.WarehouseType;
import com.example.WarehouseApp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MovementRepositoryTest {

    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private NomenclatureRepository nomenclatureRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private CounterpartyRepository counterpartyRepository;
    @Autowired
    private BatchRepository batchRepository;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll();
        batchRepository.deleteAll();
        nomenclatureRepository.deleteAll();
        warehouseRepository.deleteAll();
        counterpartyRepository.deleteAll();
    }

    @Test
    void save_shouldPersistMovement_andGenerateId() {
        var nomenclature = createNomenclature("Труба стальная", "PIPE-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var warehouseTo = createWarehouse("Склад №1", "ул. Заводская, 10", WarehouseType.REMOTE, true);
        var savedWarehouseTo = warehouseRepository.save(warehouseTo);

        var counterparty = createCounterparty("ООО Поставщик", "7701234567");
        var savedCounterparty = counterpartyRepository.save(counterparty);

        var movement = createMovement(
                MovementType.RECEIPT,
                savedNomenclature, null, null,
                null, savedWarehouseTo, null,
                savedCounterparty,
                new BigDecimal("100.50"),
                new BigDecimal("500.00"),
                10L,
                "Приход по накладной №123"
        );
        movement.setNumber("MOV-2026-000001");
        movement.setDate(LocalDate.of(2026, 10, 6));

        var saved = movementRepository.save(movement);

        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getMovementType()).isEqualTo(MovementType.RECEIPT);
        assertThat(saved.getNomenclature().getId()).isEqualTo(savedNomenclature.getId());
        assertThat(saved.getWarehouseTo().getId()).isEqualTo(savedWarehouseTo.getId());
        assertThat(saved.getCounterparty().getId()).isEqualTo(savedCounterparty.getId());
        assertThat(saved.getQuantity()).isEqualTo(new BigDecimal("100.50"));
        assertThat(saved.getPrice()).isEqualTo(new BigDecimal("500.00"));
        assertThat(saved.getNumber()).isEqualTo("MOV-2026-000001");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 10, 6));
        assertThat(saved.getUserId()).isEqualTo(10L);
    }

    @Test
    void save_shouldPersistMovement_withNullableLinks() {
        var nomenclature = createNomenclature("Гайка М12", "NUT-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var warehouseTo = createWarehouse("Склад №2", "ул. Складская, 2", WarehouseType.REMOTE, true);
        var savedWarehouseTo = warehouseRepository.save(warehouseTo);

        var movement = createMovement(
                MovementType.ISSUE,
                savedNomenclature, null, null,
                null, savedWarehouseTo, null,
                null,
                new BigDecimal("20"),
                null,
                20L,
                "Списание"
        );
        movement.setNumber("MOV-2026-000002");
        movement.setDate(LocalDate.of(2026, 10, 7));

        var saved = movementRepository.save(movement);

        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getBatch()).isNull();
        assertThat(saved.getSerialItem()).isNull();
        assertThat(saved.getStorageLocation()).isNull();
        assertThat(saved.getCounterparty()).isNull();
        assertThat(saved.getPrice()).isNull();
    }

    @Test
    void findById_shouldReturnMovement_whenExists() {
        var nomenclature = createNomenclature("Шайба плоская", "WASHER-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var warehouseTo = createWarehouse("Склад №4", "ул. Транспортная, 4", WarehouseType.REMOTE, true);
        var savedWarehouseTo = warehouseRepository.save(warehouseTo);

        var movement = createMovement(
                MovementType.TRANSFER,
                savedNomenclature, null, null,
                null, savedWarehouseTo, null,
                null,
                new BigDecimal("5"), null, 40L, "Перемещение"
        );
        movement.setNumber("MOV-FIND-001");
        movement.setDate(LocalDate.of(2026, 10, 10));

        var saved = movementRepository.save(movement);

        var found = movementRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo("MOV-FIND-001");
        assertThat(found.get().getNomenclature().getId()).isEqualTo(savedNomenclature.getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        var found = movementRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllMovements() {
        var nomenclature = createNomenclature("Кабель ВВГ", "CABLE-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var w1 = createWarehouse("Склад А", "ул. А, 1", WarehouseType.REMOTE, true);
        var w2 = createWarehouse("Склад Б", "ул. Б, 2", WarehouseType.REMOTE, true);
        var s1 = warehouseRepository.save(w1);
        var s2 = warehouseRepository.save(w2);

        var m1 = createMovement(MovementType.RECEIPT, savedNomenclature, null, null,
                null, s1, null, null, new BigDecimal("10"),
                null, 50L, "");
        m1.setNumber("MOV-ALL-001"); m1.setDate(LocalDate.of(2026, 10, 11));

        var m2 = createMovement(MovementType.ISSUE, savedNomenclature, null, null,
                null, s2, null, null, new BigDecimal("5"),
                null, 50L, "");
        m2.setNumber("MOV-ALL-002"); m2.setDate(LocalDate.of(2026, 10, 12));

        movementRepository.saveAll(List.of(m1, m2));

        var all = movementRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveMovement() {
        var nomenclature = createNomenclature("Изолента ПВХ", "TAPE-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var warehouseTo = createWarehouse("Склад В", "ул. В, 3", WarehouseType.REMOTE, true);
        var savedWarehouse = warehouseRepository.save(warehouseTo);

        var movement = createMovement(
                MovementType.WRITE_OFF,
                savedNomenclature, null, null,
                null, savedWarehouse, null,
                null,
                new BigDecimal("3"), null, 60L, "Списано"
        );
        movement.setNumber("MOV-DEL-001");
        movement.setDate(LocalDate.of(2026, 10, 13));

        var saved = movementRepository.save(movement);
        long id = saved.getId();

        movementRepository.deleteById(id);

        assertThat(movementRepository.findById(id)).isEmpty();
    }

    // Хелперы

    private Nomenclature createNomenclature(String name, String sku) {
        return new Nomenclature(
                name,
                sku,
                com.example.WarehouseApp.model.StuffCategory.MATERIAL,
                com.example.WarehouseApp.model.UnitOfMeasure.PIECE,
                com.example.WarehouseApp.model.InventoryType.BATCH,
                true
        );
    }

    private Warehouse createWarehouse(String name, String address, WarehouseType type, boolean isActive) {
        var now = LocalDateTime.now();
        var w = new Warehouse();
        w.setName(name);
        w.setAddress(address);
        w.setType(type);
        w.setActive(isActive);
        w.setCreatedAt(now);
        w.setUpdatedAt(now);
        return w;
    }

    private Counterparty createCounterparty(String name, String inn) {
        var c = new Counterparty();
        c.setName(name);
        c.setInn(inn);
        c.setKpp("770101001");
        c.setCounterpartyType(com.example.WarehouseApp.model.CounterpartyType.SUPPLIER);
        c.setPhone("89123456789");
        c.setContact("Иванов Иван");
        c.setActive(true);
        // Если у Counterparty тоже есть createdAt/updatedAt — добавь их аналогично
        return c;
    }

    private Movement createMovement(
            MovementType movementType,
            Nomenclature nomenclature,
            Batch batch,
            com.example.WarehouseApp.model.SerialItem serialItem,
            Warehouse warehouseFrom,
            Warehouse warehouseTo,
            com.example.WarehouseApp.model.StorageLocation storageLocation,
            Counterparty counterparty,
            BigDecimal quantity,
            BigDecimal price,
            long userId,
            String notes
    ) {
        var m = new Movement(movementType, quantity, price, userId, notes);
        m.setNomenclature(nomenclature);
        m.setBatch(batch);
        m.setSerialItem(serialItem);
        m.setWarehouseFrom(warehouseFrom);
        m.setWarehouseTo(warehouseTo);
        m.setStorageLocation(storageLocation);
        m.setCounterparty(counterparty);
        return m;
    }
}