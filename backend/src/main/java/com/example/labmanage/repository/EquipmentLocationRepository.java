package com.example.labmanage.repository;

import com.example.labmanage.entity.EquipmentLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentLocationRepository extends JpaRepository<EquipmentLocationEntity, Long> {

    Optional<EquipmentLocationEntity> findByCode(String code);

    List<EquipmentLocationEntity> findByStatus(String status);

    List<EquipmentLocationEntity> findByBuildingId(Long buildingId);
}
