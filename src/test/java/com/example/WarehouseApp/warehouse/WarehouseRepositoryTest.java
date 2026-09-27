package com.example.WarehouseApp.warehouse;

import com.example.WarehouseApp.model.Warehouse;
import com.example.WarehouseApp.model.WarehouseType;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class WarehouseRepositoryTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void setUp() {
        warehouseRepository.deleteAll();
    }

    @Test
    void save_shouldPersistWarehouse_andSetTimestamps() {
        Warehouse warehouse = new Warehouse(
                "Склад 1",
                "г. Москва, ул. Ленина, 1",
                WarehouseType.CENTRAL,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Warehouse saved = warehouseRepository.save(warehouse);

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getName()).isEqualTo("Склад 1");
        assertThat(saved.getType()).isEqualTo(WarehouseType.CENTRAL);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnWarehouse_whenExists() {
        Warehouse warehouse = createWarehouse("Склад 2", "г. Питер, ул. Светлая, 5", WarehouseType.REMOTE, false);
        Warehouse saved = warehouseRepository.save(warehouse);

        Optional<Warehouse> found = warehouseRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Склад 2");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Warehouse> found = warehouseRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllWarehouses() {
        Warehouse w1 = createWarehouse("Склад A", "Адрес A", WarehouseType.CENTRAL, true);
        Warehouse w2 = createWarehouse("Склад B", "Адрес B", WarehouseType.REMOTE, false);

        warehouseRepository.saveAll(List.of(w1, w2));

        List<Warehouse> all = warehouseRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveWarehouse() {
        Warehouse warehouse = createWarehouse("Склад для удаления", "Адрес удаления", WarehouseType.REMOTE, true);
        Warehouse saved = warehouseRepository.save(warehouse);

        warehouseRepository.deleteById(saved.getId());

        assertThat(warehouseRepository.findById(saved.getId())).isEmpty();
    }

    private Warehouse createWarehouse(String name, String address, WarehouseType type, boolean isActive) {
        LocalDateTime now = LocalDateTime.now();
        return new Warehouse(name, address, type, isActive, now, now);
    }
}
