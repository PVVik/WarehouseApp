package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inventory_check")
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@NoArgsConstructor
public class InventoryCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    public InventoryCheck(Warehouse warehouse, LocalDate checkDate, DocumentStatus status, Long userId) {
        this.warehouse = warehouse;
        this.checkDate = checkDate;
        this.status = status;
        this.userId = userId;
    }
}
