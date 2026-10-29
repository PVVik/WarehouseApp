package com.example.warehouseApp.model;

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
public class Batch implements EntityWithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Counterparty supplier;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    public Batch(String batchNumber, LocalDate productionDate, LocalDate expiryDate) {
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
    }

    public Batch(long id, String batchNumber, LocalDate productionDate, LocalDate expiryDate) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }
}

