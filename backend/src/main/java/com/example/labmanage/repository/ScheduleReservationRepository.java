package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ScheduleReservationRepository extends JpaRepository<ScheduleReservationEntity, Long> {
    List<ScheduleReservationEntity> findByTermIdAndDeletedFalse(Long termId);

    List<ScheduleReservationEntity> findByBookingIdAndDeletedFalse(Long bookingId);

    List<ScheduleReservationEntity> findByTeacherIdAndTermIdAndDeletedFalse(Long teacherId, Long termId);

    List<ScheduleReservationEntity> findByClazzIdAndTermIdAndDeletedFalse(Long clazzId, Long termId);

    List<ScheduleReservationEntity> findByTermIdAndWeekNoAndBuildingNameAndStatusInAndDeletedFalse(
            Long termId, Integer weekNo, String buildingName, Collection<String> statuses);

    @Query("SELECT r FROM ScheduleReservationEntity r WHERE r.termId = :termId " +
           "AND r.weekNo = :weekNo AND r.buildingName = :buildingName " +
           "AND (:roomNumber IS NULL OR r.roomNumber = :roomNumber) " +
           "AND r.status IN :statuses AND r.deleted = false")
    List<ScheduleReservationEntity> findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
            @Param("termId") Long termId,
            @Param("weekNo") Integer weekNo,
            @Param("buildingName") String buildingName,
            @Param("roomNumber") String roomNumber,
            @Param("statuses") Collection<String> statuses);

    @Query("SELECT DISTINCT r.roomNumber FROM ScheduleReservationEntity r " +
           "WHERE r.buildingName = :buildingName AND r.roomNumber IS NOT NULL ORDER BY r.roomNumber")
    List<String> findDistinctRoomNumbersByBuildingName(@Param("buildingName") String buildingName);

    @Query("SELECT DISTINCT r.buildingName, r.roomNumber FROM ScheduleReservationEntity r WHERE r.deleted = false")
    List<Object[]> findAllDistinctLabs();

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.buildingName = :buildingName AND r.roomNumber = :roomNumber " +
           "AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false")
    boolean existsByRoomTimeOverlap(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("buildingName") String buildingName,
            @Param("roomNumber") String roomNumber,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.buildingName = :buildingName AND r.roomNumber = :roomNumber " +
           "AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false AND r.id <> :excludeId")
    boolean existsByRoomTimeOverlapExcludeId(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("buildingName") String buildingName,
            @Param("roomNumber") String roomNumber,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.teacherId = :teacherId AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false")
    boolean existsByTeacherTimeOverlap(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("teacherId") Long teacherId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.teacherId = :teacherId AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false AND r.id <> :excludeId")
    boolean existsByTeacherTimeOverlapExcludeId(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("teacherId") Long teacherId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.clazzId = :clazzId AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false")
    boolean existsByClazzTimeOverlap(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("clazzId") Long clazzId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleReservationEntity r " +
           "WHERE r.clazzId = :clazzId AND r.useDate = :useDate AND r.termId = :termId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses AND r.deleted = false AND r.id <> :excludeId")
    boolean existsByClazzTimeOverlapExcludeId(
            @Param("termId") Long termId,
            @Param("useDate") LocalDate useDate,
            @Param("clazzId") Long clazzId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT r.buildingName, r.roomNumber, r.weekNo, r.dayOfWeek, r.timeSlotId, COUNT(r) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.termId = :termId AND r.weekNo = :weekNo AND r.deleted = false " +
           "AND r.status IN :statuses " +
           "GROUP BY r.buildingName, r.roomNumber, r.weekNo, r.dayOfWeek, r.timeSlotId " +
           "ORDER BY r.buildingName, r.roomNumber, r.dayOfWeek, r.timeSlotId")
    List<Object[]> countByRoomAndWeek(
            @Param("termId") Long termId,
            @Param("weekNo") Integer weekNo,
            @Param("statuses") Collection<String> statuses
    );

    @Query("SELECT r FROM ScheduleReservationEntity r WHERE r.termId = :termId " +
           "AND r.weekNo IN :weeks AND r.status IN :statuses AND r.deleted = false")
    List<ScheduleReservationEntity> findByTermIdAndWeekNoInAndStatusInAndDeletedFalse(
            @Param("termId") Long termId,
            @Param("weeks") Collection<Integer> weeks,
            @Param("statuses") Collection<String> statuses);

    List<ScheduleReservationEntity> findByTermIdAndUseDateInAndStatusInAndDeletedFalse(
            Long termId, List<LocalDate> useDates, Collection<String> statuses);

    @Query("SELECT r FROM ScheduleReservationEntity r WHERE r.teacherId = :teacherId " +
           "AND r.status = 'APPROVED' AND r.deleted = false " +
           "AND (r.useDate < CURRENT_DATE OR (r.useDate = CURRENT_DATE AND r.endTime < CURRENT_TIME))")
    List<ScheduleReservationEntity> findCompletedByTeacherId(@Param("teacherId") Long teacherId);
}
