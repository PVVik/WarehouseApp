package com.example.WarehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "serial_item")
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "serialNumber"})
@ToString
@NoArgsConstructor
public class SerialItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    private Nomenclature nomenclature;

    @Column(name = "serial_number")
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "notes")
    private String notes;


    public SerialItem(String serialNumber, ItemStatus status, String passportNumber, String notes) {
        this.serialNumber = serialNumber;
        this.status = status;
        this.passportNumber = passportNumber;
        this.notes = notes;
    }
}
