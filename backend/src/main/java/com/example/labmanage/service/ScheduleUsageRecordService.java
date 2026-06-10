package com.example.labmanage.service;

import com.example.labmanage.dto.ScheduleUsageRecordDTO;
import com.example.labmanage.entity.ScheduleReservationEntity;
import com.example.labmanage.entity.ScheduleUsageRecordEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.enums.ScheduleRegistrationStatusEnum;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.ScheduleReservationRepository;
import com.example.labmanage.repository.ScheduleUsageRecordRepository;
import com.example.labmanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleUsageRecordService {
    private final ScheduleUsageRecordRepository repository;
    private final ScheduleReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public List<ScheduleUsageRecordDTO> list() {
        return repository.findByDeletedFalse().stream().map(this::toDTO).toList();
    }

    public List<ScheduleUsageRecordDTO> listMine(Long reporterId) {
        // 1. 已有的登记记录
        List<ScheduleUsageRecordDTO> result = new ArrayList<>(
                repository.findByReporterIdAndDeletedFalse(reporterId).stream().map(this::toDTO).toList());

        // 2. 收集已有记录关联的 reservationId，用于去重
        Set<Long> existingReservationIds = new HashSet<>();
        for (ScheduleUsageRecordEntity e : repository.findByReporterIdAndDeletedFalse(reporterId)) {
            existingReservationIds.add(e.getReservationId());
        }

        // 3. 查询该老师已审批且排课时间已结束、但尚未登记的预约
        List<ScheduleReservationEntity> completed = reservationRepository.findCompletedByTeacherId(reporterId);
        for (ScheduleReservationEntity r : completed) {
            if (!existingReservationIds.contains(r.getId())) {
                result.add(toVirtualPendingDTO(r, reporterId));
            }
        }

        return result;
    }

    private ScheduleUsageRecordDTO toVirtualPendingDTO(ScheduleReservationEntity r, Long reporterId) {
        ScheduleUsageRecordDTO dto = new ScheduleUsageRecordDTO();
        dto.setId(null);  // 虚拟记录，前端据此判断走 /init 流程
        dto.setReservationId(r.getId());
        dto.setCourseId(r.getCourseId());
        dto.setClazzId(r.getClazzId());
        dto.setTimeSlotId(r.getTimeSlotId());
        dto.setBuildingName(defaultText(r.getBuildingName(), "暂无数据"));
        dto.setRoomNumber(defaultText(r.getRoomNumber(), "暂无数据"));
        dto.setLabName(buildLabName(r));
        dto.setUsageDate(r.getUseDate());
        dto.setCourseOrProjectName(defaultText(r.getProjectName(), defaultText(r.getExperimentContent(), "测试课程")));
        dto.setPlannedHours(r.getDuration() == null ? 0D : r.getDuration().doubleValue());
        dto.setClassName(defaultText(r.getClassName(), "暂无数据"));
        dto.setExpectedAttendance(resolveExpectedAttendance(r));
        dto.setReporterId(reporterId);
        dto.setDepartment("暂无数据");
        dto.setExperimentItemName("暂无数据");
        dto.setExperimentItemType("暂无数据");
        dto.setAttendanceRecord("无");
        dto.setTeachingStatus("正常");
        dto.setEquipmentStatus("正常");
        dto.setRecordStatus(ScheduleRegistrationStatusEnum.PENDING.name());
        dto.setStatus(defaultText(r.getStatus(), "APPROVED"));
        return dto;
    }

    public ScheduleUsageRecordDTO getById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public ScheduleUsageRecordDTO initForm(Long reservationId, Long reporterId) {
        ScheduleReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("预约记录不存在"));

        ScheduleUsageRecordEntity entity = repository.findByReservationIdAndDeletedFalse(reservationId)
                .stream()
                .findFirst()
                .orElseGet(ScheduleUsageRecordEntity::new);

        UserEntity reporter = userRepository.findById(reporterId).orElse(null);

        entity.setReservationId(reservation.getId());
        entity.setCourseId(reservation.getCourseId());
        entity.setClazzId(reservation.getClazzId());
        entity.setTimeSlotId(reservation.getTimeSlotId());
        entity.setDepartment("暂无数据");
        entity.setFillTime(LocalDateTime.now());
        entity.setLabName(buildLabName(reservation));
        entity.setBuildingName(defaultText(reservation.getBuildingName(), "暂无数据"));
        entity.setRoomNumber(defaultText(reservation.getRoomNumber(), "暂无数据"));
        entity.setUsageDate(reservation.getUseDate() == null ? LocalDate.now() : reservation.getUseDate());
        entity.setCourseOrProjectName(defaultText(reservation.getProjectName(), defaultText(reservation.getExperimentContent(), "测试课程")));
        entity.setPlannedHours(reservation.getDuration() == null ? 0D : reservation.getDuration().doubleValue());
        entity.setClassName(defaultText(reservation.getClassName(), "暂无数据"));
        entity.setExpectedAttendance(resolveExpectedAttendance(reservation));
        entity.setReporterId(reporterId);
        entity.setReporterName(reporter == null ? "测试用户" : defaultText(reporter.getRealName(), "测试用户"));
        entity.setExperimentItemName(defaultText(entity.getExperimentItemName(), "暂无数据"));
        entity.setExperimentItemType(defaultText(entity.getExperimentItemType(), "暂无数据"));
        entity.setAttendanceRecord(defaultText(entity.getAttendanceRecord(), "无"));
        entity.setTeachingStatus(defaultText(entity.getTeachingStatus(), "正常"));
        entity.setEquipmentStatus(defaultText(entity.getEquipmentStatus(), "正常"));
        entity.setRecordStatus(entity.getRecordStatus() == null ? ScheduleRegistrationStatusEnum.PENDING : entity.getRecordStatus());
        entity.setStatus(defaultText(entity.getStatus(), defaultText(reservation.getStatus(), "APPROVED")));
        entity.setDeleted(false);

        ScheduleUsageRecordEntity saved = repository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public ScheduleUsageRecordDTO submit(Long id, ScheduleUsageRecordDTO dto, Long reporterId) {
        ScheduleUsageRecordEntity entity = getEntity(id);
        UserEntity reporter = userRepository.findById(reporterId).orElse(null);

        entity.setReporterId(reporterId);
        if (reporter != null) {
            entity.setReporterName(defaultText(reporter.getRealName(), "测试用户"));
        } else {
            entity.setReporterName(defaultText(entity.getReporterName(), "测试用户"));
        }
        entity.setFillTime(LocalDateTime.now());
        entity.setExperimentItemName(defaultText(dto.getExperimentItemName(), "暂无数据"));
        entity.setExperimentItemType(defaultText(dto.getExperimentItemType(), "暂无数据"));
        entity.setActualHours(dto.getActualHours() == null ? 0D : dto.getActualHours());
        entity.setActualAttendance(dto.getActualAttendance() == null ? 0 : dto.getActualAttendance());
        entity.setAttendanceRecord(defaultText(dto.getAttendanceRecord(), "无"));
        entity.setTeachingStatus(defaultText(dto.getTeachingStatus(), "正常"));
        entity.setEquipmentStatus(defaultText(dto.getEquipmentStatus(), "正常"));
        entity.setRecordStatus(ScheduleRegistrationStatusEnum.REGISTERED);

        ScheduleUsageRecordEntity saved = repository.save(entity);
        return toDTO(saved);
    }

    private ScheduleUsageRecordEntity getEntity(Long id) {
        ScheduleUsageRecordEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("使用登记不存在"));
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NotFoundException("使用登记不存在");
        }
        return entity;
    }

    private ScheduleUsageRecordDTO toDTO(ScheduleUsageRecordEntity entity) {
        ScheduleUsageRecordDTO dto = new ScheduleUsageRecordDTO();
        dto.setId(entity.getId());
        dto.setReservationId(entity.getReservationId());
        dto.setCourseId(entity.getCourseId());
        dto.setClazzId(entity.getClazzId());
        dto.setTimeSlotId(entity.getTimeSlotId());
        dto.setDepartment(defaultText(entity.getDepartment(), "暂无数据"));
        dto.setFillTime(entity.getFillTime());
        dto.setLabName(defaultText(entity.getLabName(), "暂无数据"));
        dto.setBuildingName(defaultText(entity.getBuildingName(), "暂无数据"));
        dto.setRoomNumber(defaultText(entity.getRoomNumber(), "暂无数据"));
        dto.setUsageDate(entity.getUsageDate());
        dto.setCourseOrProjectName(defaultText(entity.getCourseOrProjectName(), "测试课程"));
        dto.setPlannedHours(entity.getPlannedHours() == null ? 0D : entity.getPlannedHours());
        dto.setClassName(defaultText(entity.getClassName(), "暂无数据"));
        dto.setExpectedAttendance(entity.getExpectedAttendance() == null ? 0 : entity.getExpectedAttendance());
        dto.setReporterId(entity.getReporterId());
        dto.setReporterName(defaultText(entity.getReporterName(), "测试用户"));
        dto.setExperimentItemName(defaultText(entity.getExperimentItemName(), "暂无数据"));
        dto.setExperimentItemType(defaultText(entity.getExperimentItemType(), "暂无数据"));
        dto.setActualHours(entity.getActualHours() == null ? 0D : entity.getActualHours());
        dto.setActualAttendance(entity.getActualAttendance() == null ? 0 : entity.getActualAttendance());
        dto.setAttendanceRecord(defaultText(entity.getAttendanceRecord(), "无"));
        dto.setTeachingStatus(defaultText(entity.getTeachingStatus(), "正常"));
        dto.setEquipmentStatus(defaultText(entity.getEquipmentStatus(), "正常"));
        dto.setRecordStatus(entity.getRecordStatus() == null ? ScheduleRegistrationStatusEnum.PENDING.name() : entity.getRecordStatus().name());
        dto.setStatus(defaultText(entity.getStatus(), "APPROVED"));
        return dto;
    }

    private Integer resolveExpectedAttendance(ScheduleReservationEntity reservation) {
        if (reservation.getParticipantCount() != null) {
            return reservation.getParticipantCount();
        }
        if (reservation.getStudentCount() != null) {
            return reservation.getStudentCount();
        }
        return 0;
    }

    private String buildLabName(ScheduleReservationEntity reservation) {
        String building = defaultText(reservation.getBuildingName(), "暂无数据");
        String room = defaultText(reservation.getRoomNumber(), "暂无数据");
        return building + "-" + room;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
