package com.example.WarehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "storage_location")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "warehouse_id")
    private long warehouseId;

    @Column(name = "zone")
    private String zone;

    @Column(name = "rack")
    private String rack;

    @Column(name = "shelf")
    private String shelf;

    @Column(name = "code")
    private String code;

    @Column(name = "used")
    private boolean used;

    public StorageLocation(long warehouseId, String zone, String rack, String shelf, boolean used) {
        this.warehouseId = warehouseId;
        this.zone = zone;
        this.rack = rack;
        this.shelf = shelf;
        this.code = createCode();
        this.used = used;
    }

    public String createCode() {
        return String.format("%s-%s-%s", zone, rack, shelf);
    }
}