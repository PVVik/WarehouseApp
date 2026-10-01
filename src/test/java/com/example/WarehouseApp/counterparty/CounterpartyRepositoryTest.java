package com.example.WarehouseApp.counterparty;

import com.example.WarehouseApp.model.Counterparty;
import com.example.WarehouseApp.model.CounterpartyType;
import com.example.WarehouseApp.repository.CounterpartyRepository;
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
class CounterpartyRepositoryTest {

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @BeforeEach
    void setUp() {
        counterpartyRepository.deleteAll();
    }

    @Test
    void save_shouldPersistCounterparty() {
        Counterparty counterparty = createCounterparty(
                "ООО «Ромашка»", "7801234567", "770101001",
                CounterpartyType.SUPPLIER, "+79991234567", "Иванов И.И.", true
        );

        Counterparty saved = counterpartyRepository.save(counterparty);

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getName()).isEqualTo("ООО «Ромашка»");
        assertThat(saved.getInn()).isEqualTo("7801234567");
        assertThat(saved.getKpp()).isEqualTo("770101001");
        assertThat(saved.getCounterpartyType()).isEqualTo(CounterpartyType.SUPPLIER);
        assertThat(saved.getPhone()).isEqualTo("+79991234567");
        assertThat(saved.getContact()).isEqualTo("Иванов И.И.");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void findById_shouldReturnCounterparty_whenExists() {
        Counterparty counterparty = createCounterparty(
                "ООО «Ландыш»", "7707654321", "770101002",
                CounterpartyType.CUSTOMER, "+79997654321", "Петров П.П.", false
        );
        Counterparty saved = counterpartyRepository.save(counterparty);

        Optional<Counterparty> found = counterpartyRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ООО «Ландыш»");
        assertThat(found.get().getCounterpartyType()).isEqualTo(CounterpartyType.CUSTOMER);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Counterparty> found = counterpartyRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllCounterparties() {
        Counterparty c1 = createCounterparty(
                "Поставщик А", "7701111111", "770101001",
                CounterpartyType.SUPPLIER, "+79991111111", "Иванов И.И.", true
        );
        Counterparty c2 = createCounterparty(
                "Заказчик Б", "7702222222", "770101002",
                CounterpartyType.CUSTOMER, "+79992222222", "Петров П.П.", false
        );

        counterpartyRepository.saveAll(List.of(c1, c2));

        List<Counterparty> all = counterpartyRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveCounterparty() {
        Counterparty counterparty = createCounterparty(
                "Для удаления", "7703333333", "770101003",
                CounterpartyType.CUSTOMER, "+79993333333", "Сидоров С.С.", true
        );
        Counterparty saved = counterpartyRepository.save(counterparty);

        counterpartyRepository.deleteById(saved.getId());

        assertThat(counterpartyRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsByInn_shouldReturnTrue_whenInnExists() {
        Counterparty counterparty = createCounterparty(
                "ООО «Проверка»", "7704444444", "770101004",
                CounterpartyType.SUPPLIER, "+79994444444", "Кузнецов К.К.", true
        );
        counterpartyRepository.save(counterparty);

        boolean exists = counterpartyRepository.existsByInn("7704444444");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByInn_shouldReturnFalse_whenInnNotExists() {
        boolean exists = counterpartyRepository.existsByInn("9999999999");

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldEnforceUniqueConstraint_onInn() {
        Counterparty first = createCounterparty(
                "Первый", "7705555555", "770101005",
                CounterpartyType.SUPPLIER, "+79995555555", "Морозов М.М.", true
        );
        counterpartyRepository.save(first);
        counterpartyRepository.flush();

        Counterparty duplicate = createCounterparty(
                "Второй (дубликат)", "7705555555", "770101006",
                CounterpartyType.CUSTOMER, "+79996666666", "Смирнова С.С.", false
        );

        assertThatThrownBy(() -> {
            counterpartyRepository.save(duplicate);
            counterpartyRepository.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Counterparty createCounterparty(String name, String inn, String kpp,
                                            CounterpartyType type, String phone,
                                            String contact, boolean isActive) {
        return new Counterparty(name, inn, kpp, type, phone, contact, isActive);
    }
}
