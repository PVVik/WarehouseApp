package com.example.WarehouseApp.service.movement;

import com.example.WarehouseApp.model.DocumentSequence;
import com.example.WarehouseApp.model.MovementType;
import com.example.WarehouseApp.repository.DocumentSequenceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Map;

@Service
@Transactional
public class DocumentNumberService {

    private static final Map<MovementType, String> PREFIX_MAP = Map.of(
            MovementType.RECEIPT, "PRI",
            MovementType.ISSUE, "ISS",
            MovementType.TRANSFER, "TRF",
            MovementType.WRITE_OFF, "WRO"
    );

    @Autowired
    private DocumentSequenceRepository sequenceRepo;

    public String generateNumber(MovementType type) {
        int year = Year.now().getValue();
        var prefix = PREFIX_MAP.getOrDefault(type, "DOC");

        var seq = sequenceRepo.findByMovementTypeAndYear(type, year)
                .orElseGet(() -> {
                    DocumentSequence newSeq = new DocumentSequence();
                    newSeq.setMovementType(type);
                    newSeq.setYear(year);
                    newSeq.setNextValue(1L);
                    return sequenceRepo.save(newSeq);
                });

        long currentValue = seq.getNextValue();
        seq.setNextValue(currentValue + 1);
        sequenceRepo.save(seq);

        var padded = String.format("%06d", currentValue);
        return prefix + "-" + year + "-" + padded;
    }
}
