package com.example.WarehouseApp.nomenclature;

import com.example.WarehouseApp.model.InventoryType;
import com.example.WarehouseApp.model.Nomenclature;
import com.example.WarehouseApp.model.StuffCategory;
import com.example.WarehouseApp.model.UnitOfMeasure;
import com.example.WarehouseApp.repository.NomenclatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class NomenclatureRepositoryTest {

    @Autowired
    private NomenclatureRepository nomenclatureRepository;

    @BeforeEach
    void setUp() {
        nomenclatureRepository.deleteAll();
    }

    @Test
    void save_shouldPersistNomenclature_andGenerateId() {
        Nomenclature nomenclature = createNomenclature("Болт М8", "BLT-M8-001", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);

        Nomenclature saved = nomenclatureRepository.save(nomenclature);

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getName()).isEqualTo("Болт М8");
        assertThat(saved.getSku()).isEqualTo("BLT-M8-001");
        assertThat(saved.getStuffCategory()).isEqualTo(StuffCategory.MATERIAL);
        assertThat(saved.getUnitOfMeasure()).isEqualTo(UnitOfMeasure.PIECE);
        assertThat(saved.getInventoryType()).isEqualTo(InventoryType.QUANTITY);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void findById_shouldReturnNomenclature_whenExists() {
        Nomenclature nomenclature = createNomenclature("Перчатки", "GLV-001", StuffCategory.SIZ,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);
        Nomenclature saved = nomenclatureRepository.save(nomenclature);

        Optional<Nomenclature> found = nomenclatureRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Перчатки");
        assertThat(found.get().getSku()).isEqualTo("GLV-001");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Nomenclature> found = nomenclatureRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findBySku_shouldReturnNomenclature_whenExists() {
        Nomenclature nomenclature = createNomenclature("Дизель", "FUEL-DIESEL-001",
                StuffCategory.FUEL, UnitOfMeasure.LITER, InventoryType.BATCH, true);
        nomenclatureRepository.save(nomenclature);

        Optional<Nomenclature> found = nomenclatureRepository.findBySku("FUEL-DIESEL-001");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Дизель");
        assertThat(found.get().getStuffCategory()).isEqualTo(StuffCategory.FUEL);
    }

    @Test
    void findBySku_shouldReturnEmpty_whenNotExists() {
        Optional<Nomenclature> found = nomenclatureRepository.findBySku("NOT-EXIST-999");
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllNomenclatures() {
        Nomenclature n1 = createNomenclature("Болт", "BLT-001", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);
        Nomenclature n2 = createNomenclature("Отвёртка", "Screw-001", StuffCategory.TOOL,
                UnitOfMeasure.PIECE, InventoryType.SERIAL, true);

        nomenclatureRepository.saveAll(List.of(n1, n2));

        List<Nomenclature> all = nomenclatureRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveNomenclature() {
        Nomenclature nomenclature = createNomenclature(
                "Кабель", "CAB-001", StuffCategory.EQUIPMENT, UnitOfMeasure.METER,
                InventoryType.QUANTITY, true
        );
        Nomenclature saved = nomenclatureRepository.save(nomenclature);

        nomenclatureRepository.deleteById(saved.getId());

        assertThat(nomenclatureRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void save_duplicateSku_shouldThrow() {
        Nomenclature n1 = createNomenclature("Болт", "DUP-001", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);
        nomenclatureRepository.saveAndFlush(n1);

        Nomenclature n2 = createNomenclature("Болт другой", "DUP-001", StuffCategory.EQUIPMENT,
                UnitOfMeasure.KILOGRAM, InventoryType.BATCH, false);

        assertThatThrownBy(() -> nomenclatureRepository.saveAndFlush(n2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_multipleWithDifferentSku_shouldPersistAll() {
        Nomenclature n1 = createNomenclature("Болт М8", "SKU-001", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);
        Nomenclature n2 = createNomenclature("Болт М10", "SKU-002", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);
        Nomenclature n3 = createNomenclature("Гайка М8", "SKU-003", StuffCategory.MATERIAL,
                UnitOfMeasure.PIECE, InventoryType.QUANTITY, true);

        nomenclatureRepository.saveAll(List.of(n1, n2, n3));

        List<Nomenclature> all = nomenclatureRepository.findAll();
        assertEquals(3, all.size());
    }

    private Nomenclature createNomenclature(String name, String sku, StuffCategory stuffCategory,
                                            UnitOfMeasure unitOfMeasure, InventoryType inventoryType,
                                            boolean isActive) {
        return new Nomenclature(name, sku, stuffCategory, unitOfMeasure, inventoryType, isActive);
    }
}

