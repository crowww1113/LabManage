package com.example.labmanage.repository;

import com.example.labmanage.entity.BuildingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingRepository extends JpaRepository<BuildingEntity, Long> {
    List<BuildingEntity> findAllByOrderBySortOrderAsc();
}
