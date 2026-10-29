package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "counterparty", uniqueConstraints = @UniqueConstraint(columnNames = "inn"))
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "inn"})
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Counterparty implements EntityWithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "inn", nullable = false)
    private String inn;

    @Column(name = "kpp", nullable = false)
    private String kpp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CounterpartyType counterpartyType;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "contact", nullable = false)
    private String contact;

    @Column(name = "active", nullable = false)
    private boolean isActive;

    public Counterparty(String name, String inn, String kpp, CounterpartyType counterpartyType, String phone, String contact, boolean isActive) {
        this.name = name;
        this.inn = inn;
        this.kpp = kpp;
        this.counterpartyType = counterpartyType;
        this.phone = phone;
        this.contact = contact;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }
}
