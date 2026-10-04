package com.example.WarehouseApp.serialItem;

import com.example.WarehouseApp.model.*;
import com.example.WarehouseApp.repository.SerialItemRepository;
import com.example.WarehouseApp.specification.serialItem.SerialItemSpecificationBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class SerialItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SerialItemRepository serialItemRepository;

    @Test
    void save_shouldGenerateId() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-001"));

        var item = createSerialItem(nom, null, "SN20240001", ItemStatus.NEW);
        item.setNomenclature(nom);

        var saved = serialItemRepository.saveAndFlush(item);

        assertThat(saved.getId()).isGreaterThan(0);
    }

    @Test
    void findById_shouldReturnSavedItem() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-002"));
        var item = createSerialItem(nom, null, "SN20240002", ItemStatus.IN_STOCK);
        var savedId = entityManager.persistAndFlush(item).getId();
        entityManager.clear();

        var found = serialItemRepository.findById(savedId);

        assertThat(found).isPresent();
        assertThat(found.get().getSerialNumber()).isEqualTo("SN20240002");
        assertThat(found.get().getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(found.get().getPassportNumber()).isEqualTo("П-SN20240002");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        var found = serialItemRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldPersistLinkToBatch() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-003"));
        var supplier = entityManager.persistAndFlush(createSupplier("77012345671"));
        var batch = entityManager.persistAndFlush(createBatch(nom, supplier, "BATCH_TEST_001"));

        var item = createSerialItem(nom, batch, "SN20240003", ItemStatus.IN_STOCK);
        var savedId = serialItemRepository.saveAndFlush(item).getId();
        entityManager.clear();

        var found = serialItemRepository.findById(savedId).orElseThrow();

        assertThat(found.getBatch()).isNotNull();
        assertThat(found.getBatch().getBatchNumber()).isEqualTo("BATCH_TEST_001");
        assertThat(found.getNomenclature().getSku()).isEqualTo("SKU-003");
    }

    @Test
    void save_shouldAllowNullBatch() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-004"));

        var item = createSerialItem(nom, null, "SN20240004", ItemStatus.NEW);
        var saved = serialItemRepository.saveAndFlush(item);
        entityManager.clear();

        var found = serialItemRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getBatch()).isNull();
    }

    @Test
    void update_shouldChangeStatus() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-005"));
        var item = createSerialItem(nom, null, "SN20240005", ItemStatus.IN_STOCK);
        var savedId = entityManager.persistAndFlush(item).getId();

        var found = serialItemRepository.findById(savedId).orElseThrow();
        found.setStatus(ItemStatus.IN_USE);
        serialItemRepository.saveAndFlush(found);
        entityManager.clear();

        var reloaded = serialItemRepository.findById(savedId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ItemStatus.IN_USE);
    }

    @Test
    void delete_shouldRemoveItem() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-006"));
        var item = entityManager.persistAndFlush(
                createSerialItem(nom, null, "SN20240006", ItemStatus.NEW));

        serialItemRepository.deleteById(item.getId());
        serialItemRepository.flush();

        assertThat(serialItemRepository.findById(item.getId())).isEmpty();
    }

    @Test
    void findAll_withPageable_shouldReturnPage() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-012"));
        for (int i = 1; i <= 5; i++) {
            entityManager.persist(createSerialItem(nom, null, "SN2024010" + i, ItemStatus.NEW));
        }
        entityManager.flush();

        var page = serialItemRepository.findAll((Specification<SerialItem>) null,
                PageRequest.of(0, 3, Sort.by("id")));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findAll_withSpecification_shouldFilterBySerialNumber() {
        var nom = entityManager.persistAndFlush(createNomenclature("SKU-013"));
        entityManager.persist(createSerialItem(nom, null, "SN20240020", ItemStatus.NEW));
        entityManager.persist(createSerialItem(nom, null, "SN20240021", ItemStatus.IN_USE));
        entityManager.flush();

        var builder = new SerialItemSpecificationBuilder();
        builder.with("serialNumber", ":", "SN20240020");
        var spec = builder.build();

        var result = serialItemRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(SerialItem::getSerialNumber)
                .containsExactly("SN20240020");
    }

    private Nomenclature createNomenclature(String sku) {
        return new Nomenclature("Секция насоса УЭЦН", sku,
                StuffCategory.EQUIPMENT, UnitOfMeasure.PIECE, InventoryType.SERIAL, true);
    }

    private Counterparty createSupplier(String inn) {
        return new Counterparty("ООО Поставщик Реагентов", inn, "770101001",
                CounterpartyType.SUPPLIER, "89123456789", "Иванов Иван", true);
    }

    private Batch createBatch(Nomenclature nomenclature, Counterparty supplier, String batchNumber) {
        Batch batch = new Batch(batchNumber,
                LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1), "CERT-12345");
        batch.setNomenclature(nomenclature);
        batch.setSupplier(supplier);
        return batch;
    }

    private SerialItem createSerialItem(Nomenclature nomenclature, Batch batch,
                                        String serialNumber, ItemStatus status) {
        SerialItem item = new SerialItem(serialNumber, status, "П-" + serialNumber, "Тестовая запись");
        item.setNomenclature(nomenclature);
        item.setBatch(batch);
        return item;
    }
}
