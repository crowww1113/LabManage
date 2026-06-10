package com.example.labmanage.repository;

import com.example.labmanage.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    List<CalendarEntity> findByTermId(Long termId);
    CalendarEntity findByTermIdAndDate(Long termId, LocalDate date);
}