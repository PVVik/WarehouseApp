package com.example.WarehouseApp.batch;

import com.example.WarehouseApp.model.*;
import com.example.WarehouseApp.repository.BatchRepository;
import com.example.WarehouseApp.repository.CounterpartyRepository;
import com.example.WarehouseApp.repository.NomenclatureRepository;
import com.example.WarehouseApp.specification.batch.BatchSpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"})
class BatchRepositoryTest {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private NomenclatureRepository nomenclatureRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @BeforeEach
    void setUp() {
        batchRepository.deleteAll();
        nomenclatureRepository.deleteAll();
        counterpartyRepository.deleteAll();
    }

    @Test
    void save_shouldPersistBatch_andGenerateId() {
        var nomenclature = createNomenclature("Реагент соляная кислота", "REAGENT-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var supplier = createCounterparty("ООО Поставщик", "7701234567");
        var savedSupplier = counterpartyRepository.save(supplier);

        var batch = createBatch(savedNomenclature.getId(), "BATCH-2024-001", savedSupplier.getId());
        batch.setNomenclature(savedNomenclature);
        batch.setSupplier(savedSupplier);
        Batch saved = batchRepository.save(batch);

        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getNomenclature().getId()).isEqualTo(savedNomenclature.getId());
        assertThat(saved.getBatchNumber()).isEqualTo("BATCH-2024-001");
        assertThat(saved.getSupplier().getId()).isEqualTo(savedSupplier.getId());
        assertThat(saved.getProductionDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(saved.getExpiryDate()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(saved.getCertificateNumber()).isEqualTo("CERT-12345");
    }

    @Test
    void save_shouldPersistBatch_withNullSupplier() {
        var nomenclature = createNomenclature("Инструмент универсальный", "TOOL-001");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var batch = createBatch(savedNomenclature.getId(), "BATCH-NO-SUPPLIER", null);
        var saved = batchRepository.save(batch);

        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getSupplier().getId()).isNull();
    }

    @Test
    void save_shouldFail_onDuplicateBatchNumber() {
        var nomenclature = createNomenclature("Реагент второй", "REAGENT-002");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        batchRepository.save(createBatch(savedNomenclature.getId(), "BATCH-DUP-001", null));

        assertThatThrownBy(() -> batchRepository.save(createBatch(savedNomenclature.getId(), "BATCH-DUP-001", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findById_shouldReturnBatch_whenExists() {
        var nomenclature = createNomenclature("Реагент третий", "REAGENT-003");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var saved = batchRepository.save(
                createBatch(savedNomenclature.getId(), "BATCH-FIND-001", null));

        var found = batchRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBatchNumber()).isEqualTo("BATCH-FIND-001");
        assertThat(found.get().getNomenclature().getId()).isEqualTo(savedNomenclature.getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        var found = batchRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllBatches() {
        var nomenclature = createNomenclature("Реагент четвёртый", "REAGENT-004");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var b1 = createBatch(savedNomenclature.getId(), "BATCH-ALL-001", null);
        var b2 = createBatch(savedNomenclature.getId(), "BATCH-ALL-002", null);

        batchRepository.saveAll(List.of(b1, b2));

        var all = batchRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveBatch() {
        var nomenclature = createNomenclature("Реагент пятый", "REAGENT-005");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        var saved = batchRepository.save(
                createBatch(savedNomenclature.getId(), "BATCH-DELETE-001", null));

        batchRepository.deleteById(saved.getId());

        assertThat(batchRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findAll_withSpecification_shouldFilterByBatchNumber() {
        var nomenclature = createNomenclature("Реагент шестой", "REAGENT-006");
        var savedNomenclature = nomenclatureRepository.save(nomenclature);

        batchRepository.save(createBatch(savedNomenclature.getId(), "BATCH-SPEC-001", null));
        batchRepository.save(createBatch(savedNomenclature.getId(), "BATCH-SPEC-002", null));
        batchRepository.save(createBatch(savedNomenclature.getId(), "OTHER-SPEC-003", null));

        var builder = new BatchSpecificationBuilder();
        builder.with("batchNumber", ":", "BATCH");
        var specification = builder.build();

        var result = batchRepository.findAll(specification);

        assertEquals(2, result.size());
    }

    private Nomenclature createNomenclature(String name, String sku) {
        return new Nomenclature(
                name,
                sku,
                StuffCategory.MATERIAL,
                UnitOfMeasure.KILOGRAM,
                InventoryType.BATCH,
                true
        );
    }

    private Counterparty createCounterparty(String name, String inn) {
        LocalDateTime now = LocalDateTime.now();
        return new Counterparty(
                name,
                inn,
                "770101001",
                CounterpartyType.SUPPLIER,
                "89123456789",
                "Иванов Иван",
                true
        );
    }

    private Batch createBatch(long nomenclatureId, String batchNumber, Long supplierId) {
        return new Batch(batchNumber, LocalDate.of(2024, 6, 1),
                LocalDate.of(2025, 6, 1), "CERT-12345");
    }
}
