package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ScheduleBookingRepository extends JpaRepository<ScheduleBookingEntity, Long> {
    List<ScheduleBookingEntity> findByTermIdAndDeletedFalse(Long termId);

    List<ScheduleBookingEntity> findByTeacherIdAndTermIdAndDeletedFalse(Long teacherId, Long termId);

    List<ScheduleBookingEntity> findByStatusInAndDeletedFalse(Collection<String> statuses);

    List<ScheduleBookingEntity> findByTermIdAndStatusInAndDeletedFalse(Long termId, Collection<String> statuses);

    List<ScheduleBookingEntity> findByTeachingTaskIdAndDeletedFalse(Long teachingTaskId);
}
