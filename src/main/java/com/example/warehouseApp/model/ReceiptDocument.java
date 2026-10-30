package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "receipt_document")
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDocument implements EntityWithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act_id")
    private InventoryDiscrepancyAct act;

    @Column(name = "source", nullable = false)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Column(name = "completed_by", nullable = false)
    private Long completedBy;
}
