package com.example.labmanage.service;

import com.example.labmanage.dto.*;
import com.example.labmanage.entity.*;
import com.example.labmanage.exception.BusinessException;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleBookingService {
    private static final List<String> CONFLICT_STATUSES = List.of("PENDING", "APPROVED", "IN_USE", "SUBMITTED", "APPROVING");
    private static final List<String> APPLICATION_CONFLICT_STATUSES = List.of("APPROVED", "PENDING", "IN_USE");

    private final ScheduleBookingRepository bookingRepository;
    private final ScheduleReservationRepository reservationRepository;
    private final ScheduleReservationService reservationService;
    private final ScheduleApplicationRepository applicationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TermRepository termRepository;
    private final TeachingTaskRepository teachingTaskRepository;
    private final ScheduleOperationLogService operationLogService;
    private final ScheduleNoticeService noticeService;

    // ==================== 列表 & 详情 ====================

    public List<ScheduleBookingDTO> list(Long termId, Long teacherId, String status) {
        List<ScheduleBookingEntity> list;
        if (termId != null && status != null && !status.isEmpty()) {
            list = bookingRepository.findByTermIdAndStatusInAndDeletedFalse(termId, List.of(status));
        } else if (termId != null) {
            list = bookingRepository.findByTermIdAndDeletedFalse(termId);
        } else if (status != null && !status.isEmpty()) {
            list = bookingRepository.findByStatusInAndDeletedFalse(List.of(status));
        } else {
            list = bookingRepository.findAll().stream()
                    .filter(e -> !Boolean.TRUE.equals(e.getDeleted())).toList();
        }
        return list.stream().map(this::toDTO).toList();
    }

    public ScheduleBookingDTO getById(Long id) {
        ScheduleBookingEntity booking = getEntity(id);
        ScheduleBookingDTO dto = toDTO(booking);
        List<ScheduleReservationEntity> reservations = reservationRepository.findByBookingIdAndDeletedFalse(id);
        dto.setReservations(reservations.stream().map(reservationService::toDTO).toList());
        dto.setWeeks(reservations.stream().map(ScheduleReservationEntity::getWeekNo).distinct().sorted().toList());
        return dto;
    }

    // ==================== 批量排课（主子表） ====================

    @Transactional
    public BookingBatchScheduleResponse batchCreate(BatchScheduleRequest req, Long operatorId, boolean isAdmin) {
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

        // 冲突检测（复用 reservation 级别逻辑）
        List<ScheduleApplicationEntity> approvedApps = applicationRepository
                .findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusInAndDeletedFalse(
                        task.getTermId(), req.getWeeks(), req.getDayOfWeek(), APPLICATION_CONFLICT_STATUSES);

        boolean hasSoftConflict = false;
        List<String> softConflictMsgs = new ArrayList<>();

        for (Integer week : req.getWeeks()) {
            LocalDate date = calculateDate(term.getStartDate(), week, req.getDayOfWeek());
            for (TimeSlotEntity slot : timeSlots) {
                boolean labConflict = reservationRepository.existsByRoomTimeOverlap(
                        task.getTermId(), date, req.getBuildingName(), req.getRoomNumber(),
                        slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                if (labConflict) {
                    throw new BusinessException(409,
                            String.format("实验室 %s %s 在第%d周 %s 已被占用，请重新选择",
                                    req.getBuildingName(), req.getRoomNumber(), week, slot.getSlotName()));
                }

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

                for (Long teacherId : teacherIds) {
                    boolean teacherConflict = reservationRepository.existsByTeacherTimeOverlap(
                            task.getTermId(), date, teacherId,
                            slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                    if (teacherConflict) {
                        softConflictMsgs.add(String.format("教师在第%d周 %s 已有其他安排", week, slot.getSlotName()));
                        hasSoftConflict = true;
                    }
                }

                boolean clazzConflict = reservationRepository.existsByClazzTimeOverlap(
                        task.getTermId(), date, task.getClazzId(),
                        slot.getStartTime(), slot.getEndTime(), CONFLICT_STATUSES);
                if (clazzConflict) {
                    softConflictMsgs.add(String.format("班级在第%d周 %s 已有其他课程", week, slot.getSlotName()));
                    hasSoftConflict = true;
                }
            }
        }

        if (hasSoftConflict && !req.isForce()) {
            throw new BusinessException(409,
                    "教师/班级时间冲突：" + String.join("；", softConflictMsgs) + "。是否强制排课？");
        }

        // 创建主表
        String bookingStatus = "APPROVED";
        ScheduleBookingEntity booking = new ScheduleBookingEntity();
        booking.setBookingNo("BK-" + UUID.randomUUID().toString().replace("-", ""));
        booking.setTermId(task.getTermId());
        booking.setTeachingTaskId(task.getId());
        booking.setCourseId(task.getCourseId());
        booking.setClazzId(task.getClazzId());
        booking.setTeacherId(teacherIds.get(0));
        booking.setBuildingName(req.getBuildingName());
        booking.setRoomNumber(req.getRoomNumber());
        booking.setDayOfWeek(req.getDayOfWeek());
        booking.setWeekRange(formatWeekRange(req.getWeeks()));
        booking.setTimeSlotInfo(String.join("、", timeSlots.stream()
                .map(s -> s.getSlotName() != null ? s.getSlotName() : s.getStartTime() + "-" + s.getEndTime())
                .distinct().toList()));
        booking.setStudentCount(req.getStudentCount());
        booking.setGroupCount(req.getGroupCount());
        booking.setStudentsPerGroup(req.getStudentsPerGroup());
        booking.setExperimentContent(req.getExperimentContent());
        booking.setRemark(req.getRemark());
        booking.setStatus(bookingStatus);
        booking.setDeleted(false);
        booking.setCreatedBy(operatorId);
        ScheduleBookingEntity savedBooking = bookingRepository.save(booking);

        // 创建子表
        List<ScheduleReservationEntity> entities = new ArrayList<>();
        for (Integer week : req.getWeeks()) {
            LocalDate date = calculateDate(term.getStartDate(), week, req.getDayOfWeek());
            for (TimeSlotEntity slot : timeSlots) {
                ScheduleReservationEntity entity = new ScheduleReservationEntity();
                entity.setReservationNo("RES-" + UUID.randomUUID().toString().replace("-", ""));
                entity.setBookingId(savedBooking.getId());
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
                entity.setStatus(bookingStatus);
                entity.setDeleted(false);
                entities.add(entity);
            }
        }

        List<ScheduleReservationEntity> savedReservations = reservationRepository.saveAll(entities);
        for (ScheduleReservationEntity e : savedReservations) {
            try {
                reservationService.createUsageRecordIfApproved(e);
            } catch (Exception ex) {
                System.err.println("创建使用登记记录失败（不影响排课）: " + ex.getMessage());
            }
        }

        operationLogService.createLog("BOOKING", savedBooking.getId(), "CREATE", operatorId,
                isAdmin ? "LAB_ADMIN" : "TEACHER", null, savedBooking.getStatus(),
                "集中排课批次创建", "共创建" + savedReservations.size() + "条预约");

        BookingBatchScheduleResponse response = new BookingBatchScheduleResponse();
        response.setBookingId(savedBooking.getId());
        response.setBookingNo(savedBooking.getBookingNo());
        response.setTotalReservations(savedReservations.size());
        response.setReservations(savedReservations.stream().map(reservationService::toDTO).toList());
        return response;
    }

    // ==================== 审批 ====================

    @Transactional
    public ScheduleBookingDTO approve(Long bookingId, Long operatorId) {
        ScheduleBookingEntity booking = getEntity(bookingId);
        requireStatus(booking, "PENDING");
        String beforeStatus = booking.getStatus();
        booking.setStatus("APPROVED");
        booking.setUpdatedBy(operatorId);
        bookingRepository.save(booking);

        // 级联更新子记录
        List<ScheduleReservationEntity> reservations = reservationRepository.findByBookingIdAndDeletedFalse(bookingId);
        for (ScheduleReservationEntity r : reservations) {
            if ("PENDING".equals(r.getStatus())) {
                r.setStatus("APPROVED");
                r.setUpdatedBy(operatorId);
                reservationRepository.save(r);
                try {
                    reservationService.createUsageRecordIfApproved(r);
                } catch (Exception ex) {
                    System.err.println("创建使用登记记录失败（不影响审批）: " + ex.getMessage());
                }
            }
        }

        operationLogService.createLog("BOOKING", bookingId, "APPROVE", operatorId, "LAB_ADMIN",
                beforeStatus, "APPROVED", "集中排课批次审批通过", "共" + reservations.size() + "条预约已生效");
        noticeService.createNotice("BOOKING", bookingId, operatorId, booking.getTeacherId(),
                "排课批次已通过", "您的排课批次 " + booking.getBookingNo() + " 已审核通过。", "BOOKING_STATUS");

        return toDTO(booking);
    }

    @Transactional
    public ScheduleBookingDTO reject(Long bookingId, Long operatorId, String reason) {
        ScheduleBookingEntity booking = getEntity(bookingId);
        requireStatus(booking, "PENDING");
        String beforeStatus = booking.getStatus();
        booking.setStatus("REJECTED");
        booking.setRejectReason(reason);
        booking.setUpdatedBy(operatorId);
        bookingRepository.save(booking);

        List<ScheduleReservationEntity> reservations = reservationRepository.findByBookingIdAndDeletedFalse(bookingId);
        for (ScheduleReservationEntity r : reservations) {
            if ("PENDING".equals(r.getStatus())) {
                r.setStatus("REJECTED");
                r.setRemark(reason);
                r.setUpdatedBy(operatorId);
                reservationRepository.save(r);
            }
        }

        operationLogService.createLog("BOOKING", bookingId, "REJECT", operatorId, "LAB_ADMIN",
                beforeStatus, "REJECTED", "集中排课批次驳回", reason);
        noticeService.createNotice("BOOKING", bookingId, operatorId, booking.getTeacherId(),
                "排课批次已驳回", "您的排课批次 " + booking.getBookingNo() + " 已被驳回：" + reason, "BOOKING_STATUS");

        return toDTO(booking);
    }

    @Transactional
    public ScheduleBookingDTO cancel(Long bookingId, Long operatorId, String reason) {
        ScheduleBookingEntity booking = getEntity(bookingId);
        requireStatus(booking, "PENDING", "APPROVED");
        String beforeStatus = booking.getStatus();
        booking.setStatus("CANCELLED");
        booking.setRejectReason(reason);
        booking.setUpdatedBy(operatorId);
        bookingRepository.save(booking);

        List<ScheduleReservationEntity> reservations = reservationRepository.findByBookingIdAndDeletedFalse(bookingId);
        for (ScheduleReservationEntity r : reservations) {
            if ("PENDING".equals(r.getStatus()) || "APPROVED".equals(r.getStatus())) {
                r.setStatus("CANCELLED");
                r.setRemark(reason);
                r.setUpdatedBy(operatorId);
                reservationRepository.save(r);
            }
        }

        operationLogService.createLog("BOOKING", bookingId, "CANCEL", operatorId, "LAB_ADMIN",
                beforeStatus, "CANCELLED", "集中排课批次取消", reason);

        return toDTO(booking);
    }

    // ==================== 内部方法 ====================

    private ScheduleBookingEntity getEntity(Long id) {
        return bookingRepository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .orElseThrow(() -> new NotFoundException("排课批次不存在"));
    }

    private void requireStatus(ScheduleBookingEntity entity, String... statuses) {
        for (String status : statuses) {
            if (status.equals(entity.getStatus())) return;
        }
        throw new BusinessException(400, "当前状态不允许执行该操作");
    }

    private LocalDate calculateDate(LocalDate termStart, int weekNo, int dayOfWeek) {
        LocalDate mondayOfWeek1 = termStart.with(DayOfWeek.MONDAY);
        return mondayOfWeek1.plusWeeks(weekNo - 1).plusDays(dayOfWeek - 1);
    }

    private ScheduleBookingDTO toDTO(ScheduleBookingEntity entity) {
        ScheduleBookingDTO dto = new ScheduleBookingDTO();
        dto.setId(entity.getId());
        dto.setBookingNo(entity.getBookingNo());
        dto.setTermId(entity.getTermId());
        dto.setTeachingTaskId(entity.getTeachingTaskId());
        dto.setCourseId(entity.getCourseId());
        dto.setClazzId(entity.getClazzId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setBuildingName(entity.getBuildingName());
        dto.setRoomNumber(entity.getRoomNumber());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setWeekRange(entity.getWeekRange());
        dto.setTimeSlotInfo(entity.getTimeSlotInfo());
        dto.setStudentCount(entity.getStudentCount());
        dto.setGroupCount(entity.getGroupCount());
        dto.setStudentsPerGroup(entity.getStudentsPerGroup());
        dto.setExperimentContent(entity.getExperimentContent());
        dto.setRemark(entity.getRemark());
        dto.setProjectName(entity.getProjectName());
        dto.setProjectCategory(entity.getProjectCategory());
        dto.setProjectLeader(entity.getProjectLeader());
        dto.setContactPhone(entity.getContactPhone());
        dto.setGrade(entity.getGrade());
        dto.setClassName(entity.getClassName());
        dto.setParticipantCount(entity.getParticipantCount());
        dto.setDuration(entity.getDuration());
        dto.setStatus(entity.getStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private String formatWeekRange(List<Integer> weeks) {
        if (weeks == null || weeks.isEmpty()) return "";
        List<Integer> sorted = weeks.stream().sorted().toList();
        StringBuilder sb = new StringBuilder();
        int start = sorted.get(0), prev = start;
        for (int i = 1; i < sorted.size(); i++) {
            int cur = sorted.get(i);
            if (cur == prev + 1) {
                prev = cur;
            } else {
                if (sb.length() > 0) sb.append(",");
                sb.append(start == prev ? String.valueOf(start) : start + "-" + prev);
                start = cur;
                prev = cur;
            }
        }
        if (sb.length() > 0) sb.append(",");
        sb.append(start == prev ? String.valueOf(start) : start + "-" + prev);
        return sb.toString();
    }
}
