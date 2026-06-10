package com.example.labmanage.service;

import com.example.labmanage.dto.BatchScheduleRequest;
import com.example.labmanage.dto.BatchScheduleResponse;
import com.example.labmanage.dto.LabAvailabilityItem;
import com.example.labmanage.dto.ScheduleMatrixDTO;
import com.example.labmanage.dto.ScheduleReservationDTO;
import com.example.labmanage.dto.TimeSlotDTO;
import com.example.labmanage.dto.UpdateScheduleRequest;
import com.example.labmanage.dto.WeeklyRoomUsageSummaryDTO;
import com.example.labmanage.entity.*;
import com.example.labmanage.enums.ScheduleRegistrationStatusEnum;
import com.example.labmanage.exception.BusinessException;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleReservationService {
    private static final List<String> CONFLICT_STATUSES = List.of("PENDING", "APPROVED", "IN_USE", "SUBMITTED", "APPROVING");
    private static final List<String> APPLICATION_CONFLICT_STATUSES = List.of("APPROVED", "PENDING", "IN_USE");

    private final ScheduleReservationRepository repository;
    private final ScheduleApplicationRepository applicationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TermRepository termRepository;
    private final TeachingTaskRepository teachingTaskRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClazzRepository clazzRepository;
    private final ScheduleUsageRecordRepository usageRecordRepository;
    private final ScheduleOperationLogService operationLogService;
    private final ScheduleNoticeService noticeService;

    public List<ScheduleReservationDTO> list(Long termId, Long teacherId, Long clazzId) {
        List<ScheduleReservationEntity> list;
        if (termId != null && teacherId != null) {
            list = repository.findByTeacherIdAndTermIdAndDeletedFalse(teacherId, termId);
        } else if (termId != null && clazzId != null) {
            list = repository.findByClazzIdAndTermIdAndDeletedFalse(clazzId, termId);
        } else if (termId != null) {
            list = repository.findByTermIdAndDeletedFalse(termId);
        } else {
            list = repository.findAll().stream().filter(entity -> !Boolean.TRUE.equals(entity.getDeleted())).toList();
        }
        return list.stream().map(this::toDTO).toList();
    }

    public ScheduleReservationDTO getById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public ScheduleReservationDTO create(ScheduleReservationDTO dto) {
        validateConflicts(dto, null);
        ScheduleReservationEntity entity = new ScheduleReservationEntity();
        copy(dto, entity);
        entity.setReservationNo("RES-" + UUID.randomUUID().toString().replace("-", ""));
        entity.setStatus(dto.getStatus() == null ? "PENDING" : dto.getStatus());
        entity.setDeleted(false);
        ScheduleReservationEntity saved = repository.save(entity);
        try { createUsageRecordIfApproved(saved); } catch (Exception ex) { System.err.println("创建使用登记失败: " + ex.getMessage()); }
        operationLogService.createLog("RESERVATION", saved.getId(), "CREATE", saved.getTeacherId(), "STUDENT",
                null, saved.getStatus(), "创建预约单", "创建成功");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO update(Long id, ScheduleReservationDTO dto) {
        ScheduleReservationEntity entity = getEntity(id);
        validateConflicts(dto, id);
        copy(dto, entity);
        ScheduleReservationEntity saved = repository.save(entity);
        try { createUsageRecordIfApproved(saved); } catch (Exception ex) { System.err.println("创建使用登记失败: " + ex.getMessage()); }
        operationLogService.createLog("RESERVATION", saved.getId(), "UPDATE", saved.getTeacherId(), "STUDENT",
                null, saved.getStatus(), "更新预约单", "更新成功");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO updateReservation(Long id, UpdateScheduleRequest request, Long currentUserId) {
        if (request.getWeeks().size() > 1 || request.getTimeSlotIds().size() > 1) {
            throw new BusinessException(400, "单次修改仅支持选择单个周次和节次");
        }

        ScheduleReservationEntity entity = getEntity(id);

        TermEntity term = termRepository.findById(entity.getTermId())
                .orElseThrow(() -> new NotFoundException("学期不存在"));

        Integer newWeekNo = request.getWeeks().get(0);
        Long newTimeSlotId = request.getTimeSlotIds().get(0);

        TimeSlotEntity timeSlot = timeSlotRepository.findById(newTimeSlotId)
                .orElseThrow(() -> new NotFoundException("节次不存在"));

        LocalDate newUseDate = calculateDate(term.getStartDate(), newWeekNo, request.getDayOfWeek());

        // 防自己撞自己：排除当前 id 做冲突检测
        boolean labConflict = repository.existsByRoomTimeOverlapExcludeId(
                entity.getTermId(), newUseDate, request.getBuildingName(), request.getRoomNumber(),
                timeSlot.getStartTime(), timeSlot.getEndTime(), CONFLICT_STATUSES, id);
        if (labConflict) {
            throw new BusinessException(409, "该实验室在所选时间段已被占用，请重新选择");
        }

        boolean teacherConflict = repository.existsByTeacherTimeOverlapExcludeId(
                entity.getTermId(), newUseDate, entity.getTeacherId(),
                timeSlot.getStartTime(), timeSlot.getEndTime(), CONFLICT_STATUSES, id);
        if (teacherConflict) {
            throw new BusinessException(409, "该教师在所选时间段已有其他安排");
        }

        boolean clazzConflict = repository.existsByClazzTimeOverlapExcludeId(
                entity.getTermId(), newUseDate, entity.getClazzId(),
                timeSlot.getStartTime(), timeSlot.getEndTime(), CONFLICT_STATUSES, id);
        if (clazzConflict) {
            throw new BusinessException(409, "该班级在所选时间段已有其他课程");
        }

        // 更新排课参数
        entity.setWeekNo(newWeekNo);
        entity.setDayOfWeek(request.getDayOfWeek());
        entity.setTimeSlotId(newTimeSlotId);
        entity.setUseDate(newUseDate);
        entity.setStartTime(timeSlot.getStartTime());
        entity.setEndTime(timeSlot.getEndTime());
        entity.setBuildingName(request.getBuildingName());
        entity.setRoomNumber(request.getRoomNumber());
        entity.setUpdatedBy(currentUserId);

        ScheduleReservationEntity saved = repository.save(entity);

        operationLogService.createLog("RESERVATION", saved.getId(), "UPDATE", currentUserId, "LAB_ADMIN",
                entity.getStatus(), saved.getStatus(), "管理员修改了排课时间和地点", "修改成功");

        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        ScheduleReservationEntity entity = getEntity(id);
        entity.setDeleted(true);
        repository.save(entity);
        operationLogService.createLog("RESERVATION", entity.getId(), "DELETE", entity.getUpdatedBy(), "SYSTEM",
                entity.getStatus(), entity.getStatus(), "删除预约单", "删除成功");
    }

    @Transactional
    public ScheduleReservationDTO approve(Long id, Long operatorId) {
        ScheduleReservationEntity entity = getEntity(id);
        requireStatus(entity, "PENDING");
        String beforeStatus = entity.getStatus();
        entity.setStatus("APPROVED");
        entity.setUpdatedBy(operatorId);
        ScheduleReservationEntity saved = repository.save(entity);

        // 审批通过时自动创建使用登记记录
        try {
            createUsageRecordIfApproved(saved);
        } catch (Exception ex) {
            System.err.println("创建使用登记记录失败（不影响审批）: " + ex.getMessage());
        }

        operationLogService.createLog("RESERVATION", saved.getId(), "APPROVE", operatorId, "LAB_ADMIN",
                beforeStatus, saved.getStatus(), "审核通过预约单", "处理成功");
        noticeService.createNotice("RESERVATION", saved.getId(), operatorId, saved.getTeacherId(),
                "预约单已通过", "您的预约单已审核通过。", "RESERVATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO reject(Long id, Long operatorId, String reason) {
        ScheduleReservationEntity entity = getEntity(id);
        requireStatus(entity, "PENDING");
        String beforeStatus = entity.getStatus();
        entity.setStatus("REJECTED");
        entity.setRemark(reason);
        entity.setUpdatedBy(operatorId);
        ScheduleReservationEntity saved = repository.save(entity);
        operationLogService.createLog("RESERVATION", saved.getId(), "REJECT", operatorId, "LAB_ADMIN",
                beforeStatus, saved.getStatus(), "驳回预约单", reason == null ? "处理成功" : reason);
        noticeService.createNotice("RESERVATION", saved.getId(), operatorId, saved.getTeacherId(),
                "预约单已驳回", reason == null ? "您的预约单未通过审核。" : reason, "RESERVATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO cancel(Long id, Long operatorId, String reason) {
        ScheduleReservationEntity entity = getEntity(id);
        requireStatus(entity, "PENDING", "APPROVED");
        String beforeStatus = entity.getStatus();
        entity.setStatus("CANCELLED");
        entity.setRemark(reason);
        entity.setUpdatedBy(operatorId);
        ScheduleReservationEntity saved = repository.save(entity);
        operationLogService.createLog("RESERVATION", saved.getId(), "CANCEL", operatorId, "LAB_ADMIN",
                beforeStatus, saved.getStatus(), "取消预约单", reason == null ? "处理成功" : reason);
        noticeService.createNotice("RESERVATION", saved.getId(), operatorId, saved.getTeacherId(),
                "预约单已取消", reason == null ? "您的预约单已取消。" : reason, "RESERVATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO startUse(Long id, Long operatorId) {
        ScheduleReservationEntity entity = getEntity(id);
        requireStatus(entity, "APPROVED");
        String beforeStatus = entity.getStatus();
        entity.setStatus("IN_USE");
        entity.setUpdatedBy(operatorId);
        ScheduleReservationEntity saved = repository.save(entity);
        operationLogService.createLog("RESERVATION", saved.getId(), "START_USE", operatorId, "TEACHER",
                beforeStatus, saved.getStatus(), "开始使用实验室", "处理成功");
        noticeService.createNotice("RESERVATION", saved.getId(), operatorId, saved.getTeacherId(),
                "实验室开始使用", "您的预约单已进入使用中状态。", "RESERVATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleReservationDTO complete(Long id, Long operatorId) {
        ScheduleReservationEntity entity = getEntity(id);
        requireStatus(entity, "IN_USE");
        String beforeStatus = entity.getStatus();
        entity.setStatus("COMPLETED");
        entity.setUpdatedBy(operatorId);
        ScheduleReservationEntity saved = repository.save(entity);
        operationLogService.createLog("RESERVATION", saved.getId(), "COMPLETE", operatorId, "TEACHER",
                beforeStatus, saved.getStatus(), "完成预约单", "处理成功");
        noticeService.createNotice("RESERVATION", saved.getId(), operatorId, saved.getTeacherId(),
                "预约单已完成", "您的预约单已完成。", "RESERVATION_STATUS");
        return toDTO(saved);
    }

    private void validateConflicts(ScheduleReservationDTO dto, Long currentId) {
        boolean labConflict = currentId == null
                ? repository.existsByRoomTimeOverlap(
                dto.getTermId(), dto.getUseDate(), dto.getBuildingName(), dto.getRoomNumber(),
                dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES)
                : repository.existsByRoomTimeOverlapExcludeId(
                dto.getTermId(), dto.getUseDate(), dto.getBuildingName(), dto.getRoomNumber(),
                dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES, currentId);
        if (labConflict) {
            throw new BusinessException(409, "该实验室在所选时间段已被占用，请重新选择");
        }

        boolean teacherConflict = false;
        if (dto.getTeacherId() != null) {
            teacherConflict = currentId == null
                    ? repository.existsByTeacherTimeOverlap(
                    dto.getTermId(), dto.getUseDate(), dto.getTeacherId(),
                    dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES)
                    : repository.existsByTeacherTimeOverlapExcludeId(
                    dto.getTermId(), dto.getUseDate(), dto.getTeacherId(),
                    dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES, currentId);
        }
        if (teacherConflict) {
            throw new BusinessException(409, "该教师在所选时间段已有其他安排");
        }

        boolean clazzConflict = false;
        if (dto.getClazzId() != null) {
            clazzConflict = currentId == null
                    ? repository.existsByClazzTimeOverlap(
                    dto.getTermId(), dto.getUseDate(), dto.getClazzId(),
                    dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES)
                    : repository.existsByClazzTimeOverlapExcludeId(
                    dto.getTermId(), dto.getUseDate(), dto.getClazzId(),
                    dto.getStartTime(), dto.getEndTime(), CONFLICT_STATUSES, currentId);
        }
        if (clazzConflict) {
            throw new BusinessException(409, "该班级在所选时间段已有其他课程");
        }
    }

    private ScheduleReservationEntity getEntity(Long id) {
        ScheduleReservationEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("预约单不存在"));
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NotFoundException("预约单不存在");
        }
        return entity;
    }

    private void requireStatus(ScheduleReservationEntity entity, String... statuses) {
        for (String status : statuses) {
            if (status.equals(entity.getStatus())) {
                return;
            }
        }
        throw new BusinessException(400, "当前状态不允许执行该操作");
    }

    public void createUsageRecordIfApproved(ScheduleReservationEntity saved) {
        if ("APPROVED".equals(saved.getStatus())) {
            List<ScheduleUsageRecordEntity> existing = usageRecordRepository.findByReservationIdAndDeletedFalse(saved.getId());
            if (existing != null && !existing.isEmpty()) {
                return;
            }
            ScheduleUsageRecordEntity usageRecord = new ScheduleUsageRecordEntity();
            usageRecord.setReservationId(saved.getId());
            usageRecord.setCourseId(saved.getCourseId());
            usageRecord.setClazzId(saved.getClazzId());
            usageRecord.setCourseOrProjectName(saved.getProjectName() != null ? saved.getProjectName() : "");
            usageRecord.setClassName(saved.getClassName() != null ? saved.getClassName() : "");
            usageRecord.setReporterId(saved.getTeacherId());
            usageRecord.setBuildingName(saved.getBuildingName());
            usageRecord.setRoomNumber(saved.getRoomNumber());
            usageRecord.setLabName(saved.getBuildingName() + " " + saved.getRoomNumber());
            usageRecord.setUsageDate(saved.getUseDate());
            usageRecord.setTimeSlotId(saved.getTimeSlotId());
            usageRecord.setExpectedAttendance(saved.getStudentCount());
            usageRecord.setPlannedHours(saved.getDuration() != null ? saved.getDuration().doubleValue() : 0.0);
            usageRecord.setRecordStatus(ScheduleRegistrationStatusEnum.PENDING);
            usageRecord.setStatus(saved.getStatus());
            usageRecord.setDeleted(false);
            usageRecordRepository.save(usageRecord);
        }
    }

    private void copy(ScheduleReservationDTO dto, ScheduleReservationEntity entity) {
        entity.setTermId(dto.getTermId());
        entity.setTeachingTaskId(dto.getTeachingTaskId());
        entity.setApplicationId(dto.getApplicationId());
        entity.setBookingId(dto.getBookingId());
        entity.setCourseId(dto.getCourseId());
        entity.setClazzId(dto.getClazzId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setBuildingName(dto.getBuildingName());
        entity.setRoomNumber(dto.getRoomNumber());
        entity.setUseDate(dto.getUseDate());
        entity.setWeekNo(dto.getWeekNo());
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setTimeSlotId(dto.getTimeSlotId());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setStudentCount(dto.getStudentCount());
        entity.setGroupCount(dto.getGroupCount());
        entity.setStudentsPerGroup(dto.getStudentsPerGroup());
        entity.setExperimentContent(dto.getExperimentContent());
        entity.setRemark(dto.getRemark());
        // 新增人性化字段
        entity.setProjectName(dto.getProjectName());
        entity.setProjectCategory(dto.getProjectCategory());
        entity.setProjectLeader(dto.getProjectLeader());
        entity.setContactPhone(dto.getContactPhone());
        entity.setGrade(dto.getGrade());
        entity.setClassName(dto.getClassName());
        entity.setParticipantCount(dto.getParticipantCount());
        entity.setDuration(dto.getDuration());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    public ScheduleReservationDTO toDTO(ScheduleReservationEntity entity) {
        ScheduleReservationDTO dto = new ScheduleReservationDTO();
        dto.setId(entity.getId());
        dto.setReservationNo(entity.getReservationNo());
        dto.setTermId(entity.getTermId());
        dto.setTeachingTaskId(entity.getTeachingTaskId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setBookingId(entity.getBookingId());
        dto.setCourseId(entity.getCourseId());
        dto.setClazzId(entity.getClazzId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setBuildingName(entity.getBuildingName());
        dto.setRoomNumber(entity.getRoomNumber());
        dto.setUseDate(entity.getUseDate());
        dto.setWeekNo(entity.getWeekNo());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setTimeSlotId(entity.getTimeSlotId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStudentCount(entity.getStudentCount());
        dto.setGroupCount(entity.getGroupCount());
        dto.setStudentsPerGroup(entity.getStudentsPerGroup());
        dto.setExperimentContent(entity.getExperimentContent());
        dto.setRemark(entity.getRemark());
        // 新增人性化字段
        dto.setProjectName(entity.getProjectName());
        dto.setProjectCategory(entity.getProjectCategory());
        dto.setProjectLeader(entity.getProjectLeader());
        dto.setContactPhone(entity.getContactPhone());
        dto.setGrade(entity.getGrade());
        dto.setClassName(entity.getClassName());
        dto.setParticipantCount(entity.getParticipantCount());
        dto.setDuration(entity.getDuration());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public List<WeeklyRoomUsageSummaryDTO> getWeeklySummary(Long termId, Integer weekNo) {
        List<String> activeStatuses = List.of("PENDING", "APPROVED", "IN_USE", "COMPLETED");
        List<Object[]> rows = repository.countByRoomAndWeek(termId, weekNo, activeStatuses);

        // 按楼宇+房间分组
        Map<String, List<Object[]>> grouped = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] + "|" + row[1];
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        List<WeeklyRoomUsageSummaryDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Object[]>> entry : grouped.entrySet()) {
            List<Object[]> roomRows = entry.getValue();
            Object[] first = roomRows.get(0);

            WeeklyRoomUsageSummaryDTO summary = new WeeklyRoomUsageSummaryDTO();
            summary.setBuildingName((String) first[0]);
            summary.setRoomNumber((String) first[1]);
            summary.setWeekNo((Integer) first[2]);

            // 构建 detailMap: dayOfWeek -> timeSlotId -> count
            Map<Integer, Map<Long, Long>> detailMap = new TreeMap<>();
            long total = 0;
            for (Object[] row : roomRows) {
                int dayOfWeek = (Integer) row[3];
                Long timeSlotId = (Long) row[4];
                Long count = (Long) row[5];
                detailMap.computeIfAbsent(dayOfWeek, k -> new TreeMap<>()).put(timeSlotId, count);
                total += count;
            }
            summary.setTotalSlots(total);
            summary.setDetailMap(detailMap);

            // 构建矩阵
            ScheduleMatrixDTO matrix = buildMatrix(summary.getBuildingName(), summary.getRoomNumber(), weekNo, roomRows);
            summary.setMatrixList(List.of(matrix));

            result.add(summary);
        }
        return result;
    }

    public ScheduleMatrixDTO getScheduleMatrix(Long termId, Integer weekNo, String buildingName, String roomNumber) {
        List<String> activeStatuses = List.of("PENDING", "APPROVED", "IN_USE", "COMPLETED");
        List<Object[]> rows = repository.countByRoomAndWeek(termId, weekNo, activeStatuses);
        List<Object[]> filtered = rows.stream()
                .filter(r -> buildingName.equals(r[0]) && roomNumber.equals(r[1]))
                .toList();
        return buildMatrix(buildingName, roomNumber, weekNo, filtered);
    }

    // ==================== 集中排课：实验室可用性推演 ====================

    public List<LabAvailabilityItem> getLabAvailability(Long termId, List<Integer> weeks, Integer dayOfWeek,
                                                        List<Long> timeSlotIds, String buildingName,
                                                        Long excludeReservationId) {
        TermEntity term = termRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("学期不存在"));
        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllById(timeSlotIds);
        if (timeSlots.size() != timeSlotIds.size()) {
            throw new BusinessException(400, "存在无效的节次ID");
        }

        List<LocalDate> dates = weeks.stream()
                .map(w -> calculateDate(term.getStartDate(), w, dayOfWeek))
                .toList();

        List<String[]> knownLabs = getAllKnownLabs(buildingName);

        List<ScheduleReservationEntity> reservations = repository
                .findByTermIdAndWeekNoInAndStatusInAndDeletedFalse(termId, weeks, CONFLICT_STATUSES);

        // 排除自身：修改场景下忽略当前正在编辑的预约记录
        if (excludeReservationId != null) {
            reservations = reservations.stream()
                    .filter(r -> !r.getId().equals(excludeReservationId))
                    .toList();
        }

        List<ScheduleApplicationEntity> approvedApps = applicationRepository
                .findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusInAndDeletedFalse(
                        termId, weeks, dayOfWeek, APPLICATION_CONFLICT_STATUSES);

        Map<Long, TimeSlotEntity> timeSlotMap = timeSlotRepository.findAll()
                .stream().collect(Collectors.toMap(TimeSlotEntity::getId, t -> t));

        Map<Long, String> courseNameCache = new HashMap<>();
        Map<Long, String> userNameCache = new HashMap<>();
        Map<Long, String> clazzNameCache = new HashMap<>();

        List<LabAvailabilityItem> result = new ArrayList<>();
        for (String[] lab : knownLabs) {
            String bn = lab[0];
            String rn = lab[1];
            List<LabAvailabilityItem.ConflictInfo> conflicts = new ArrayList<>();

            for (Integer week : weeks) {
                LocalDate date = calculateDate(term.getStartDate(), week, dayOfWeek);
                for (TimeSlotEntity slot : timeSlots) {
                    // 检查 reservation 冲突
                    for (ScheduleReservationEntity r : reservations) {
                        if (r.getBuildingName().equals(bn) && r.getRoomNumber().equals(rn)
                                && r.getWeekNo().equals(week)
                                && r.getStartTime().isBefore(slot.getEndTime())
                                && r.getEndTime().isAfter(slot.getStartTime())) {
                            conflicts.add(buildConflictInfo(r.getWeekNo(), r.getTimeSlotId(), r.getCourseId(),
                                    r.getTeacherId(), r.getClazzId(), "RESERVATION",
                                    timeSlotMap, courseNameCache, userNameCache, clazzNameCache));
                        }
                    }
                    // 检查 application 冲突
                    for (ScheduleApplicationEntity a : approvedApps) {
                        if (bn.equals(a.getPreferredBuildingName()) && rn.equals(a.getPreferredRoomNumber())
                                && week.equals(a.getPreferredWeekNo())
                                && a.getPreferredTimeSlotId() != null) {
                            TimeSlotEntity appSlot = timeSlotMap.get(a.getPreferredTimeSlotId());
                            if (appSlot != null
                                    && appSlot.getStartTime().isBefore(slot.getEndTime())
                                    && appSlot.getEndTime().isAfter(slot.getStartTime())) {
                                // ScheduleApplicationEntity 已移除 courseId 和 clazzId，传入 null
                                conflicts.add(buildConflictInfo(a.getPreferredWeekNo(), a.getPreferredTimeSlotId(),
                                        null, a.getTeacherId(), null, "APPLICATION",
                                        timeSlotMap, courseNameCache, userNameCache, clazzNameCache));
                            }
                        }
                    }
                }
            }

            LabAvailabilityItem item = new LabAvailabilityItem();
            item.setBuildingName(bn);
            item.setRoomNumber(rn);
            item.setAvailable(conflicts.isEmpty());
            item.setConflicts(conflicts.isEmpty() ? null : conflicts);
            result.add(item);
        }
        return result;
    }

    // ==================== 集中排课：批量排课 ====================

    @Transactional
    public BatchScheduleResponse batchCreate(BatchScheduleRequest req, Long operatorId, boolean isAdmin) {
        TeachingTaskEntity task = teachingTaskRepository.findById(req.getTaskId())
                .orElseThrow(() -> new NotFoundException("教学任务不存在"));
        List<Long> teacherIds = task.getTeacherIds();
        if (teacherIds.isEmpty()) {
            throw new BusinessException(400, "教学任务未分配教师");
        }

        TermEntity term = termRepository.findById(task.getTermId())
                .orElseThrow(() -> new NotFoundException("学期不存在"));

        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllById(req.getTimeSlotIds());
        if (timeSlots.size() != req.getTimeSlotIds().size()) {
            throw new BusinessException(400, "存在无效的节次ID");
        }

        // 预加载申请（用于硬冲突检测）
        List<ScheduleApplicationEntity> approvedApps = applicationRepository
                .findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusInAndDeletedFalse(
                        task.getTermId(), req.getWeeks(), req.getDayOfWeek(), APPLICATION_CONFLICT_STATUSES);

        boolean hasSoftConflict = false;
        List<String> softConflictMsgs = new ArrayList<>();

        // 遍历 (周次 x 节次) 全排列
        for (Integer week : req.getWeeks()) {
            LocalDate date = calculateDate(term.getStartDate(), week, req.getDayOfWeek());
            for (TimeSlotEntity slot : timeSlots) {
                // ---- 硬冲突：实验室占用 ----
                boolean labConflict = repository.existsByRoomTimeOverlap(
                        task.getTermId(), date, req.getBuildingName(), req.getRoomNumber(),
                        slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                if (labConflict) {
                    throw new BusinessException(409,
                            String.format("实验室 %s %s 在第%d周 %s 已被占用，请重新选择",
                                    req.getBuildingName(), req.getRoomNumber(), week, slot.getSlotName()));
                }

                // 检查已审批申请中的实验室冲突
                for (ScheduleApplicationEntity a : approvedApps) {
                    if (req.getBuildingName().equals(a.getPreferredBuildingName())
                            && req.getRoomNumber().equals(a.getPreferredRoomNumber())
                            && week.equals(a.getPreferredWeekNo())
                            && a.getPreferredTimeSlotId() != null) {
                        TimeSlotEntity appSlot = timeSlots.stream()
                                .filter(ts -> ts.getId().equals(a.getPreferredTimeSlotId())).findFirst().orElse(null);
                        if (appSlot == null) {
                            appSlot = timeSlotRepository.findById(a.getPreferredTimeSlotId()).orElse(null);
                        }
                        if (appSlot != null
                                && appSlot.getStartTime().isBefore(slot.getEndTime())
                                && appSlot.getEndTime().isAfter(slot.getStartTime())) {
                            throw new BusinessException(409,
                                    String.format("实验室 %s %s 在第%d周 %s 已被申请占用",
                                            req.getBuildingName(), req.getRoomNumber(), week, slot.getSlotName()));
                        }
                    }
                }

                // ---- 软冲突：教师时间 ----
                for (Long teacherId : teacherIds) {
                    boolean teacherConflict = repository.existsByTeacherTimeOverlap(
                            task.getTermId(), date, teacherId,
                            slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                    if (teacherConflict) {
                        String msg = String.format("教师在第%d周 %s 已有其他安排", week, slot.getSlotName());
                        softConflictMsgs.add(msg);
                        hasSoftConflict = true;
                    }
                }

                // ---- 软冲突：班级时间 ----
                boolean clazzConflict = repository.existsByClazzTimeOverlap(
                        task.getTermId(), date, task.getClazzId(),
                        slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                if (clazzConflict) {
                    String msg = String.format("班级在第%d周 %s 已有其他课程", week, slot.getSlotName());
                    softConflictMsgs.add(msg);
                    hasSoftConflict = true;
                }
            }
        }

        if (hasSoftConflict && !req.isForce()) {
            throw new BusinessException(409,
                    "教师/班级时间冲突：" + String.join("；", softConflictMsgs) + "。是否强制排课？");
        }

        // ---- 数据入库 ----
        List<ScheduleReservationEntity> entities = new ArrayList<>();
        for (Integer week : req.getWeeks()) {
            LocalDate date = calculateDate(term.getStartDate(), week, req.getDayOfWeek());
            for (TimeSlotEntity slot : timeSlots) {
                ScheduleReservationEntity entity = new ScheduleReservationEntity();
                entity.setReservationNo("RES-" + UUID.randomUUID().toString().replace("-", ""));
                entity.setTermId(task.getTermId());
                entity.setTeachingTaskId(task.getId());
                entity.setCourseId(task.getCourseId());
                entity.setClazzId(task.getClazzId());
                entity.setTeacherId(teacherIds.get(0));
                entity.setBuildingName(req.getBuildingName());
                entity.setRoomNumber(req.getRoomNumber());
                entity.setUseDate(date);
                entity.setWeekNo(week);
                entity.setDayOfWeek(req.getDayOfWeek());
                entity.setTimeSlotId(slot.getId());
                entity.setStartTime(slot.getStartTime());
                entity.setEndTime(slot.getEndTime());
                entity.setStudentCount(req.getStudentCount());
                entity.setGroupCount(req.getGroupCount());
                entity.setStudentsPerGroup(req.getStudentsPerGroup());
                entity.setExperimentContent(req.getExperimentContent());
                entity.setRemark(req.getRemark());
                entity.setStatus("APPROVED");
                entity.setDeleted(false);
                entities.add(entity);
            }
        }

        List<ScheduleReservationEntity> saved = repository.saveAll(entities);

        for (ScheduleReservationEntity e : saved) {
            try { createUsageRecordIfApproved(e); } catch (Exception ex) { System.err.println("创建使用登记失败: " + ex.getMessage()); }
        }

        BatchScheduleResponse response = new BatchScheduleResponse();
        response.setTotalCount(saved.size());
        response.setReservationNos(saved.stream().map(ScheduleReservationEntity::getReservationNo).toList());
        response.setReservations(saved.stream().map(this::toDTO).toList());
        return response;
    }

    // ==================== 私有辅助方法 ====================

    private List<String[]> getAllKnownLabs(String buildingName) {
        // 合并 reservation 和 application 两张表的教室列表
        List<Object[]> rows = repository.findAllDistinctLabs();
        Set<String> seen = new HashSet<>();
        List<String[]> labs = new ArrayList<>();
        for (Object[] row : rows) {
            String bName = (String) row[0];
            String rNumber = (String) row[1];
            if (buildingName != null && !buildingName.isBlank() && !buildingName.equals(bName)) {
                continue;
            }
            String key = bName + "|" + rNumber;
            if (seen.add(key)) {
                labs.add(new String[]{bName, rNumber});
            }
        }
        // 补充 application 表中的教室
        if (buildingName != null && !buildingName.isBlank()) {
            List<String> appRooms = applicationRepository.findDistinctPreferredRoomNumbersByBuildingName(buildingName);
            for (String rNumber : appRooms) {
                String key = buildingName + "|" + rNumber;
                if (seen.add(key)) {
                    labs.add(new String[]{buildingName, rNumber});
                }
            }
        }
        return labs;
    }

    private LocalDate calculateDate(LocalDate termStart, int weekNo, int dayOfWeek) {
        LocalDate mondayOfWeek1 = termStart.with(DayOfWeek.MONDAY);
        return mondayOfWeek1.plusWeeks(weekNo - 1).plusDays(dayOfWeek - 1);
    }

    private LabAvailabilityItem.ConflictInfo buildConflictInfo(Integer weekNo, Long timeSlotId, Long courseId,
                                                                Long teacherId, Long clazzId, String sourceType,
                                                                Map<Long, TimeSlotEntity> timeSlotMap,
                                                                Map<Long, String> courseNameCache,
                                                                Map<Long, String> userNameCache,
                                                                Map<Long, String> clazzNameCache) {
        LabAvailabilityItem.ConflictInfo info = new LabAvailabilityItem.ConflictInfo();
        info.setWeekNo(weekNo);
        TimeSlotEntity slot = timeSlotMap.get(timeSlotId);
        info.setTimeSlotName(slot != null ? slot.getSlotName() : "未知节次");
        info.setCourseName(resolveCourseName(courseId, courseNameCache));
        info.setTeacherName(resolveTeacherName(teacherId, userNameCache));
        info.setClazzName(resolveClazzName(clazzId, clazzNameCache));
        info.setSourceType(sourceType);
        return info;
    }

    private String resolveCourseName(Long courseId, Map<Long, String> courseNameCache) {
        if (courseId == null) {
            return "未知课程";
        }
        return courseNameCache.computeIfAbsent(courseId,
                id -> courseRepository.findById(id).map(Course::getCnName).orElse("未知课程"));
    }

    private String resolveTeacherName(Long teacherId, Map<Long, String> userNameCache) {
        if (teacherId == null) {
            return "未知教师";
        }
        return userNameCache.computeIfAbsent(teacherId,
                id -> userRepository.findById(id).map(UserEntity::getRealName).orElse("未知教师"));
    }

    private String resolveClazzName(Long clazzId, Map<Long, String> clazzNameCache) {
        if (clazzId == null) {
            return "未知班级";
        }
        return clazzNameCache.computeIfAbsent(clazzId,
                id -> clazzRepository.findById(id).map(ClazzEntity::getClazzName).orElse("未知班级"));
    }

    private ScheduleMatrixDTO buildMatrix(String buildingName, String roomNumber, Integer weekNo, List<Object[]> rows) {
        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllByOrderBySortOrderAsc();
        int slotCount = timeSlots.size();

        ScheduleMatrixDTO dto = new ScheduleMatrixDTO();
        dto.setBuildingName(buildingName);
        dto.setRoomNumber(roomNumber);
        dto.setWeekNo(weekNo);
        dto.setTimeSlots(timeSlots.stream().map(this::toTimeSlotDTO).toList());

        // 初始化 7天 x slotCount 矩阵
        Integer[][] matrix = new Integer[7][slotCount];
        for (int i = 0; i < 7; i++) {
            Arrays.fill(matrix[i], 0);
        }

        // 填充数据
        for (Object[] row : rows) {
            int dayOfWeek = (Integer) row[3];
            Long timeSlotId = (Long) row[4];
            Long count = (Long) row[5];
            // 找到 timeSlot 的索引
            int slotIndex = -1;
            for (int i = 0; i < timeSlots.size(); i++) {
                if (timeSlots.get(i).getId().equals(timeSlotId)) {
                    slotIndex = i;
                    break;
                }
            }
            if (slotIndex >= 0 && dayOfWeek >= 1 && dayOfWeek <= 7) {
                matrix[dayOfWeek - 1][slotIndex] = count.intValue();
            }
        }
        dto.setMatrix(matrix);
        return dto;
    }

    private TimeSlotDTO toTimeSlotDTO(TimeSlotEntity entity) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(entity.getId());
        dto.setSlotName(entity.getSlotName());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }
}
