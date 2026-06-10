package com.example.labmanage.repository;

import com.example.labmanage.entity.EquipmentRepairRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepairRecordRepository extends JpaRepository<EquipmentRepairRecordEntity, Long> {
    List<EquipmentRepairRecordEntity> findByEquipmentId(Long equipmentId);
}
