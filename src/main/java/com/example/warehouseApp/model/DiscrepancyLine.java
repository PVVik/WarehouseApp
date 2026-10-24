package com.example.warehouseApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "discrepancy_line")
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString
@NoArgsConstructor
public class DiscrepancyLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act_id", nullable = false)
    private InventoryDiscrepancyAct act;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "accounted_qty", nullable = false)
    private BigDecimal accountedQty;

    @Column(name = "actual_qty", nullable = false)
    private BigDecimal actualQty;

    @Column(name = "difference", nullable = false)
    private BigDecimal difference;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private DiscrepancyType type;
}
