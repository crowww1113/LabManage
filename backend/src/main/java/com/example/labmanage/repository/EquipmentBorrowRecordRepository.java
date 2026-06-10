package com.example.labmanage.repository;

import com.example.labmanage.entity.EquipmentBorrowRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface EquipmentBorrowRecordRepository extends JpaRepository<EquipmentBorrowRecordEntity, Long> {
    List<EquipmentBorrowRecordEntity> findByStatus(String status);

    @Query("SELECT r FROM EquipmentBorrowRecordEntity r WHERE r.status = '已借出' AND r.expectedReturnDate <= :date")
    List<EquipmentBorrowRecordEntity> findOverdueRecords(@Param("date") LocalDate date);

    @Query("SELECT r FROM EquipmentBorrowRecordEntity r WHERE r.status = '已借出' AND r.expectedReturnDate = :date")
    List<EquipmentBorrowRecordEntity> findDueTodayRecords(@Param("date") LocalDate date);

    @Query("SELECT r FROM EquipmentBorrowRecordEntity r WHERE r.status = '已借出' AND r.expectedReturnDate = :date")
    List<EquipmentBorrowRecordEntity> findDueTomorrowRecords(@Param("date") LocalDate date);

    List<EquipmentBorrowRecordEntity> findByEquipmentId(Long equipmentId);

    List<EquipmentBorrowRecordEntity> findByBorrowerId(Long borrowerId);
}