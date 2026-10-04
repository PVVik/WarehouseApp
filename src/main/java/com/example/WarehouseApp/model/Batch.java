package com.example.WarehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "batch")
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "batchNumber"})
@ToString
@NoArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    private Nomenclature nomenclature;

    @Column(name = "batch_number")
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Counterparty supplier;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;


    public Batch(String batchNumber, LocalDate productionDate, LocalDate expiryDate, String certificateNumber) {
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.certificateNumber = certificateNumber;
    }

    public Batch(long id, String batchNumber, LocalDate productionDate, LocalDate expiryDate, String certificateNumber) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.certificateNumber = certificateNumber;
    }
}

