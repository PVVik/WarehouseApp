package com.example.WarehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nomenclature", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
@Getter
@Setter
@EqualsAndHashCode(of = {"id", "sku"})
@ToString
@NoArgsConstructor
public class Nomenclature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private StuffCategory stuffCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InventoryType inventoryType;

    @Column(name = "active", nullable = false)
    private boolean isActive;

    public Nomenclature(String name, String sku, StuffCategory stuffCategory, UnitOfMeasure unitOfMeasure,
                        InventoryType inventoryType, boolean isActive) {
        this.name = name;
        this.sku = sku;
        this.stuffCategory = stuffCategory;
        this.unitOfMeasure = unitOfMeasure;
        this.inventoryType = inventoryType;
        this.isActive = isActive;
    }
}
