package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "storage_location")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString
public class StorageLocation implements EntityWithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "warehouse_id", nullable = false)
    private long warehouseId;

    @Column(name = "zone", nullable = false)
    private String zone;

    @Column(name = "rack")
    private String rack;

    @Column(name = "shelf")
    private String shelf;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "used", nullable = false)
    private boolean used;

    public StorageLocation(long warehouseId, String zone, String rack, String shelf, boolean used) {
        this.warehouseId = warehouseId;
        this.zone = zone;
        this.rack = rack;
        this.shelf = shelf;
        this.code = createCode();
        this.used = used;
    }

    public StorageLocation(long id, long warehouseId, String zone, String rack, String shelf, boolean used) {
        this.id = id;
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