package com.example.WarehouseApp.model;

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
public class Counterparty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "inn")
    private String inn;

    @Column(name = "kpp")
    private String kpp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CounterpartyType counterpartyType;

    @Column(name = "phone")
    private String phone;

    @Column(name = "contact")
    private String contact;

    @Column(name = "active")
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
}
