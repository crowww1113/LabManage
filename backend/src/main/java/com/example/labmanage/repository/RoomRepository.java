package com.example.labmanage.repository;

import com.example.labmanage.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    List<RoomEntity> findByBuildingIdOrderByFloorAscCodeAsc(Long buildingId);

    List<RoomEntity> findByBuildingId(Long buildingId);
}
