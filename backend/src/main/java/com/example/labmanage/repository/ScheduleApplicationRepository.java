package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleApplicationRepository extends JpaRepository<ScheduleApplicationEntity, Long> {
    List<ScheduleApplicationEntity> findByDeletedFalse();

    List<ScheduleApplicationEntity> findByTeacherIdAndTermIdAndDeletedFalse(Long teacherId, Long termId);

    List<ScheduleApplicationEntity> findByStatusAndDeletedFalse(String status);

    Optional<ScheduleApplicationEntity> findByTeachingTaskIdAndStatusAndDeletedFalse(Long teachingTaskId, String status);

    List<ScheduleApplicationEntity> findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusAndDeletedFalse(
            Long termId, Integer weekNo, String buildingName, String status);

    List<ScheduleApplicationEntity> findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusInAndDeletedFalse(
            Long termId, Integer weekNo, String buildingName, Collection<String> statuses);

    List<ScheduleApplicationEntity> findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusInAndDeletedFalse(
            Long termId, Collection<Integer> weeks, Integer dayOfWeek, Collection<String> statuses);

    List<ScheduleApplicationEntity> findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusAndDeletedFalse(
            Long termId, Collection<Integer> weeks, Integer dayOfWeek, String status);

    @Query("SELECT a FROM ScheduleApplicationEntity a WHERE a.termId = :termId " +
           "AND a.preferredWeekNo = :weekNo AND a.preferredBuildingName = :buildingName " +
           "AND (:roomNumber IS NULL OR a.preferredRoomNumber = :roomNumber) " +
           "AND a.status IN :statuses AND a.deleted = false")
    List<ScheduleApplicationEntity> findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
            @Param("termId") Long termId,
            @Param("weekNo") Integer weekNo,
            @Param("buildingName") String buildingName,
            @Param("roomNumber") String roomNumber,
            @Param("statuses") Collection<String> statuses);

    @Query("SELECT DISTINCT a.preferredRoomNumber FROM ScheduleApplicationEntity a " +
           "WHERE a.preferredBuildingName = :buildingName AND a.preferredRoomNumber IS NOT NULL")
    List<String> findDistinctPreferredRoomNumbersByBuildingName(@Param("buildingName") String buildingName);
}
