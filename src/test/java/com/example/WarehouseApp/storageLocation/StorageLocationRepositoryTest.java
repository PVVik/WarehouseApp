package com.example.WarehouseApp.storageLocation;

import com.example.WarehouseApp.model.StorageLocation;
import com.example.WarehouseApp.model.Warehouse;
import com.example.WarehouseApp.model.WarehouseType;
import com.example.WarehouseApp.repository.StorageLocationRepository;
import com.example.WarehouseApp.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"})
class StorageLocationRepositoryTest {

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void setUp() {
        storageLocationRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    void save_shouldPersistStorageLocation_andGenerateCode() {
        Warehouse warehouse = createWarehouse("Центральный склад", "г. Москва, ул. Ленина, 1", WarehouseType.CENTRAL, true);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        long warehouseId = savedWarehouse.getId();

        StorageLocation location = new StorageLocation(warehouseId, "A", "01", "02", false);
        StorageLocation saved = storageLocationRepository.save(location);

        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getWarehouseId()).isEqualTo(warehouseId);
        assertThat(saved.getZone()).isEqualTo("A");
        assertThat(saved.getRack()).isEqualTo("01");
        assertThat(saved.getShelf()).isEqualTo("02");
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getCode()).isEqualTo("A-01-02");
    }

    @Test
    void findById_shouldReturnStorageLocation_whenExists() {
        Warehouse warehouse = createWarehouse("Склад 2", "г. Питер, ул. Светлая, 5", WarehouseType.REMOTE, false);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        StorageLocation location = new StorageLocation(savedWarehouse.getId(), "B", "10", "05", true);
        StorageLocation saved = storageLocationRepository.save(location);

        Optional<StorageLocation> found = storageLocationRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getZone()).isEqualTo("B");
        assertThat(found.get().getCode()).isEqualTo("B-10-05");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<StorageLocation> found = storageLocationRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllLocations() {
        Warehouse w = createWarehouse("Тестовый склад", "Адрес склада", WarehouseType.CENTRAL, true);
        Warehouse savedW = warehouseRepository.save(w);

        StorageLocation l1 = new StorageLocation(savedW.getId(), "A", "01", "01", false);
        StorageLocation l2 = new StorageLocation(savedW.getId(), "A", "01", "02", true);

        storageLocationRepository.saveAll(List.of(l1, l2));

        List<StorageLocation> all = storageLocationRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getCode()).isEqualTo("A-01-01");
        assertThat(all.get(1).getCode()).isEqualTo("A-01-02");
    }

    @Test
    void deleteById_shouldRemoveStorageLocation() {
        Warehouse w = createWarehouse("Склад для удаления", "Адрес удаления", WarehouseType.REMOTE, true);
        Warehouse savedW = warehouseRepository.save(w);

        StorageLocation location = new StorageLocation(savedW.getId(), "C", "99", "99", false);
        StorageLocation saved = storageLocationRepository.save(location);

        storageLocationRepository.deleteById(saved.getId());

        assertThat(storageLocationRepository.findById(saved.getId())).isEmpty();
    }

    private Warehouse createWarehouse(String name, String address, WarehouseType type, boolean isActive) {
        LocalDateTime now = LocalDateTime.now();
        return new Warehouse(name, address, type, isActive, now, now);
    }
}
