package com.example.labmanage.repository;

import com.example.labmanage.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, Long> {
    List<TimeSlotEntity> findAllByOrderBySortOrderAsc();
}