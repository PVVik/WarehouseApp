package com.example.warehouseApp.repository;

import com.example.warehouseApp.dto.reports.MovementReportProjection;
import com.example.warehouseApp.dto.reports.TurnoverProjection;
import com.example.warehouseApp.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {

    @Query("""
            SELECT
                m.date AS date,
                m.movementType AS documentType,
                m.number AS documentNumber,
                m.nomenclature.name AS nomenclatureName,
                m.quantity AS qty
            FROM Movement m
            WHERE (:warehouseId IS NULL OR m.warehouseFrom.id = :warehouseId OR m.warehouseTo.id = :warehouseId)
              AND m.date BETWEEN :startDate AND :endDate
            ORDER BY m.date DESC
            """)
    List<MovementReportProjection> findReportByWarehouseAndPeriod(
            @Param("warehouseId") Long warehouseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT 
              n.id AS nomenclatureId, 
              n.name AS nomenclatureName, 
              SUM(m.quantity) AS totalQuantity, 
              SUM(m.quantity * m.price) AS totalAmount, 
              (SUM(m.quantity) * 30.0 / :daysInPeriod) AS avgMonthlyQuantity 
            FROM Movement m 
            JOIN m.nomenclature n 
            WHERE m.date BETWEEN :start AND :end 
              AND m.movementType IN ('ISSUE', 'WRITE_OFF') 
              AND (:warehouseId IS NULL OR m.warehouseFrom.id = :warehouseId) 
            GROUP BY n.id, n.name
            """)
    List<TurnoverProjection> findTurnoverDataByPeriod(
            @Param("warehouseId") Long warehouseId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("daysInPeriod") double daysInPeriod);

}
