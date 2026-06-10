package com.example.labmanage.repository;

import com.example.labmanage.entity.EquipmentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategoryEntity, Long> {

    Optional<EquipmentCategoryEntity> findByCode(String code);

    List<EquipmentCategoryEntity> findByParentId(Long parentId);

    List<EquipmentCategoryEntity> findByStatus(String status);
}
