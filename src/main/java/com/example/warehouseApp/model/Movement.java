package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movement")
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "number"})
@ToString
@NoArgsConstructor
public class Movement {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_item_id")
    private SerialItem serialItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_from")
    private Warehouse warehouseFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_to")
    private Warehouse warehouseTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id")
    private StorageLocation storageLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private Counterparty counterparty;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "document_number", nullable = false)
    private String number;

    @Column(name = "document_date", nullable = false)
    private LocalDate date;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "notes")
    private String notes;

    public Movement(MovementType movementType, BigDecimal quantity, BigDecimal price, long userId, String notes) {
        this.movementType = movementType;
        this.quantity = quantity;
        this.price = price;
        this.userId = userId;
        this.notes = notes;
    }

    public Movement(MovementType movementType, BigDecimal quantity, long userId, String notes) {
        this.movementType = movementType;
        this.quantity = quantity;
        this.userId = userId;
        this.notes = notes;
    }

}
