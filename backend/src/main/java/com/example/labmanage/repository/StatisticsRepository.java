package com.example.labmanage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.labmanage.entity.ScheduleReservationEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<ScheduleReservationEntity, Long> {

    // 1. 楼宇×房间×周次 统计（按 room 粒度的预约槽位数）
    @Query("SELECT r.buildingName, r.roomNumber, r.weekNo, " +
           "COUNT(DISTINCT CONCAT(CAST(r.dayOfWeek AS string), '-', CAST(r.timeSlotId AS string))) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY r.buildingName, r.roomNumber, r.weekNo " +
           "ORDER BY r.buildingName, r.roomNumber, r.weekNo")
    List<Object[]> countReservedSlotsByRoomAndWeek(@Param("termId") Long termId);

    // 2. 楼宇×房间 人数统计（联查 usage_record）
    @Query("SELECT ur.buildingName, ur.roomNumber, " +
           "COALESCE(SUM(CASE WHEN ur.expectedAttendance IS NOT NULL THEN ur.expectedAttendance ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN ur.actualAttendance IS NOT NULL THEN ur.actualAttendance ELSE 0 END), 0) " +
           "FROM ScheduleUsageRecordEntity ur " +
           "WHERE ur.reservationId IN (" +
           "  SELECT r.id FROM ScheduleReservationEntity r " +
           "  WHERE r.termId = :termId AND r.deleted = false " +
           "  AND r.status IN ('APPROVED','IN_USE','COMPLETED')" +
           ") AND ur.deleted = false " +
           "GROUP BY ur.buildingName, ur.roomNumber " +
           "ORDER BY ur.buildingName, ur.roomNumber")
    List<Object[]> aggregateHeadcountByRoom(@Param("termId") Long termId);

    // 3. 专业维度课时统计
    @Query("SELECT m.majorName, COUNT(r) " +
           "FROM ScheduleReservationEntity r, TeachingTaskEntity tt, MajorEntity m " +
           "WHERE r.teachingTaskId = tt.id AND tt.clazzId IN (" +
           "  SELECT c.id FROM ClazzEntity c WHERE c.majorId = m.id" +
           ") AND r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY m.majorName ORDER BY m.majorName")
    List<Object[]> aggregateHoursByMajor(@Param("termId") Long termId);

    // 4. 班级维度课时统计
    @Query("SELECT r.clazzId, c.clazzName, COUNT(r) " +
           "FROM ScheduleReservationEntity r LEFT JOIN ClazzEntity c ON r.clazzId = c.id " +
           "WHERE r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY r.clazzId, c.clazzName " +
           "ORDER BY c.clazzName")
    List<Object[]> aggregateHoursByClazz(@Param("termId") Long termId);

    // 5. 年级维度课时统计
    @Query("SELECT r.grade, COUNT(r) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY r.grade ORDER BY r.grade")
    List<Object[]> aggregateHoursByGrade(@Param("termId") Long termId);

    // 6. 课程维度课时统计
    @Query("SELECT c.id, c.cnName, COUNT(r) " +
           "FROM ScheduleReservationEntity r, TeachingTaskEntity tt, Course c " +
           "WHERE r.teachingTaskId = tt.id AND tt.courseId = c.id " +
           "AND r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY c.id, c.cnName ORDER BY c.cnName")
    List<Object[]> aggregateHoursByCourse(@Param("termId") Long termId);

    // 7. 项目类别维度预约统计
    @Query("SELECT r.projectCategory, COUNT(r), SUM(r.duration) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.termId = :termId AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "GROUP BY r.projectCategory ORDER BY r.projectCategory")
    List<Object[]> aggregateByProjectCategory(@Param("termId") Long termId);

    // 8. 已完成预约（登记率分母）
    @Query("SELECT r.id, r.teacherId FROM ScheduleReservationEntity r " +
           "WHERE r.termId = :termId AND r.deleted = false AND r.status = 'COMPLETED'")
    List<Object[]> findCompletedReservationsForRate(@Param("termId") Long termId);

    // 9. 已登记的预约ID集合
    @Query("SELECT DISTINCT ur.reservationId FROM ScheduleUsageRecordEntity ur " +
           "WHERE ur.deleted = false AND ur.recordStatus = 'REGISTERED' " +
           "AND ur.reservationId IN :reservationIds")
    List<Long> findRegisteredReservationIds(@Param("reservationIds") List<Long> reservationIds);

    // 10. 教师姓名（通过 userId → username 映射）
    @Query("SELECT u.id, u.realName FROM UserEntity u WHERE u.id IN :userIds")
    List<Object[]> findUserNamesByIds(@Param("userIds") List<Long> userIds);

    // ========== Dashboard 大屏查询 ==========

    // D1. 当天所有预约（用于实时占用判断）
    @Query("SELECT r.buildingName, r.roomNumber, r.startTime, r.endTime " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.useDate = :today AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED')")
    List<Object[]> findTodayReservations(@Param("today") LocalDate today);

    // D2. 排课密度（楼宇×日期 COUNT）
    @Query("SELECT r.buildingName, CAST(r.useDate AS string), COUNT(r) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.deleted = false AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "AND (:termId IS NULL OR r.termId = :termId) " +
           "AND (:startDate IS NULL OR r.useDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.useDate <= :endDate) " +
           "GROUP BY r.buildingName, r.useDate ORDER BY r.buildingName, r.useDate")
    List<Object[]> aggregateDensity(@Param("termId") Long termId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    // D3. 使用率趋势（按周次分组，受日期范围限制）
    @Query("SELECT r.buildingName, r.roomNumber, r.weekNo, " +
           "COUNT(DISTINCT CONCAT(CAST(r.dayOfWeek AS string), '-', CAST(r.timeSlotId AS string))) " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.deleted = false AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "AND (:termId IS NULL OR r.termId = :termId) " +
           "AND (:startDate IS NULL OR r.useDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.useDate <= :endDate) " +
           "GROUP BY r.buildingName, r.roomNumber, r.weekNo " +
           "ORDER BY r.weekNo, r.buildingName, r.roomNumber")
    List<Object[]> aggregateTrend(@Param("termId") Long termId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    // D4. 分类占比（按申请类型 COUNT）
    @Query("SELECT COALESCE(a.applicationType, '未指定'), COUNT(r) " +
           "FROM ScheduleReservationEntity r LEFT JOIN ScheduleApplicationEntity a ON r.applicationId = a.id " +
           "WHERE r.deleted = false AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "AND (:termId IS NULL OR r.termId = :termId) " +
           "AND (:startDate IS NULL OR r.useDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.useDate <= :endDate) " +
           "GROUP BY a.applicationType ORDER BY COUNT(r) DESC")
    List<Object[]> aggregateByApplicationType(@Param("termId") Long termId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    // D5. 登记完成率（按部门×状态分组）
    @Query("SELECT ur.department, ur.recordStatus, COUNT(ur) " +
           "FROM ScheduleUsageRecordEntity ur " +
           "WHERE ur.deleted = false " +
           "AND ur.reservationId IN (" +
           "  SELECT r.id FROM ScheduleReservationEntity r " +
           "  WHERE r.termId = :termId AND r.deleted = false " +
           "  AND r.status IN ('APPROVED','IN_USE','COMPLETED')" +
           ") GROUP BY ur.department, ur.recordStatus ORDER BY ur.department")
    List<Object[]> aggregateRegistrationByDept(@Param("termId") Long termId);

    // D6a. 逾期未登记 Top N
    @Query("SELECT ur.courseOrProjectName, ur.department, ur.reporterName, " +
           "CAST(ur.usageDate AS string), ur.buildingName, ur.roomNumber, ur.recordStatus " +
           "FROM ScheduleUsageRecordEntity ur " +
           "WHERE ur.deleted = false AND ur.recordStatus = 'OVERDUE' " +
           "AND ur.reservationId IN (" +
           "  SELECT r.id FROM ScheduleReservationEntity r " +
           "  WHERE r.termId = :termId AND r.deleted = false" +
           ") ORDER BY ur.usageDate DESC")
    List<Object[]> findOverdueAlerts(@Param("termId") Long termId);

    // D6b. 设备异常 Top N（teachingStatus 或 equipmentStatus 非正常）
    @Query("SELECT ur.courseOrProjectName, ur.department, ur.reporterName, " +
           "CAST(ur.usageDate AS string), ur.buildingName, ur.roomNumber, " +
           "ur.teachingStatus, ur.equipmentStatus " +
           "FROM ScheduleUsageRecordEntity ur " +
           "WHERE ur.deleted = false " +
           "AND (ur.teachingStatus <> '正常' OR ur.equipmentStatus <> '正常') " +
           "AND ur.reservationId IN (" +
           "  SELECT r.id FROM ScheduleReservationEntity r " +
           "  WHERE r.termId = :termId AND r.deleted = false" +
           ") ORDER BY ur.usageDate DESC")
    List<Object[]> findEquipmentAlerts(@Param("termId") Long termId);

    // D7. 当前时段占用（精确实时查询）
    @Query("SELECT DISTINCT r.buildingName, r.roomNumber " +
           "FROM ScheduleReservationEntity r " +
           "WHERE r.useDate = :today AND r.deleted = false " +
           "AND r.status IN ('APPROVED','IN_USE','COMPLETED') " +
           "AND r.startTime < :nowTime AND r.endTime > :nowTime")
    List<Object[]> findCurrentlyOccupiedRooms(@Param("today") LocalDate today,
                                               @Param("nowTime") LocalTime nowTime);
}
