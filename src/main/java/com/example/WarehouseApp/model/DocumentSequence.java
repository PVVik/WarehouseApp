package com.example.WarehouseApp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "document_sequence")
@NoArgsConstructor
public class DocumentSequence {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "movement_type")
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "year")
    private int year;

    @Column(name = "next_value")
    private long nextValue;
}
