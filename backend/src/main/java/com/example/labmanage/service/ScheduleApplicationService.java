package com.example.labmanage.service;

import com.example.labmanage.dto.ScheduleApplicationDTO;
import com.example.labmanage.entity.ScheduleApplicationEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.ScheduleApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {
    private final ScheduleApplicationRepository repository;
    private final ScheduleOperationLogService operationLogService;
    private final ScheduleNoticeService noticeService;

    public List<ScheduleApplicationDTO> list(Long termId, Long teacherId) {
        List<ScheduleApplicationEntity> list;
        if (termId != null && teacherId != null) {
            list = repository.findByTeacherIdAndTermIdAndDeletedFalse(teacherId, termId);
        } else {
            list = repository.findByDeletedFalse();
        }
        return list.stream().map(this::toDTO).toList();
    }

    public ScheduleApplicationDTO getById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public ScheduleApplicationDTO create(ScheduleApplicationDTO dto, Long currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("无法获取当前登录用户，请重新登录");
        }
        if (dto.getTermId() == null) {
            throw new IllegalArgumentException("学期ID不能为空，请检查教学任务学期信息");
        }
        ScheduleApplicationEntity entity = new ScheduleApplicationEntity();
        copy(dto, entity);
        entity.setTeacherId(currentUserId);
        entity.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
        entity.setStatus(dto.getStatus() == null ? "DRAFT" : dto.getStatus());
        entity.setDeleted(false);
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "CREATE", saved.getTeacherId(), "TEACHER",
                null, saved.getStatus(), "创建授课申请单", "创建成功");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleApplicationDTO update(Long id, ScheduleApplicationDTO dto) {
        ScheduleApplicationEntity entity = getEntity(id);
        copy(dto, entity);
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "UPDATE", saved.getTeacherId(), "TEACHER",
                null, saved.getStatus(), "更新授课申请单", "更新成功");
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        ScheduleApplicationEntity entity = getEntity(id);
        entity.setDeleted(true);
        repository.save(entity);
        operationLogService.createLog("APPLICATION", entity.getId(), "DELETE", entity.getUpdatedBy(), "SYSTEM",
                entity.getStatus(), entity.getStatus(), "删除授课申请单", "删除成功");
    }

    @Transactional
    public ScheduleApplicationDTO submit(Long id) {
        ScheduleApplicationEntity entity = getEntity(id);
        requireStatus(entity, "DRAFT");
        String beforeStatus = entity.getStatus();
        entity.setStatus("SUBMITTED");
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "SUBMIT", saved.getTeacherId(), "TEACHER",
                beforeStatus, saved.getStatus(), "提交授课申请单", "提交成功");
        noticeService.createNotice("APPLICATION", saved.getId(), saved.getTeacherId(), saved.getTeacherId(),
                "授课申请已提交", "您的授课申请单已提交，等待管理员审核。", "APPLICATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleApplicationDTO approve(Long id, String reviewComment, Long reviewerId) {
        ScheduleApplicationEntity entity = getEntity(id);
        requireStatus(entity, "SUBMITTED", "APPROVING");
        String beforeStatus = entity.getStatus();
        entity.setStatus("APPROVED");
        entity.setReviewComment(reviewComment);
        entity.setReviewedBy(reviewerId);
        entity.setReviewedAt(LocalDateTime.now());
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "APPROVE", reviewerId, "LAB_ADMIN",
                beforeStatus, saved.getStatus(), "审核通过授课申请单", reviewComment == null ? "处理成功" : reviewComment);
        noticeService.createNotice("APPLICATION", saved.getId(), reviewerId, saved.getTeacherId(),
                "授课申请已通过", "您的授课申请单已审核通过。", "APPLICATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleApplicationDTO reject(Long id, String reviewComment, Long reviewerId) {
        ScheduleApplicationEntity entity = getEntity(id);
        requireStatus(entity, "SUBMITTED", "APPROVING");
        String beforeStatus = entity.getStatus();
        entity.setStatus("REJECTED");
        entity.setReviewComment(reviewComment);
        entity.setReviewedBy(reviewerId);
        entity.setReviewedAt(LocalDateTime.now());
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "REJECT", reviewerId, "LAB_ADMIN",
                beforeStatus, saved.getStatus(), "驳回授课申请单", reviewComment == null ? "处理成功" : reviewComment);
        noticeService.createNotice("APPLICATION", saved.getId(), reviewerId, saved.getTeacherId(),
                "授课申请已驳回", reviewComment == null ? "您的授课申请单未通过审核。" : reviewComment, "APPLICATION_STATUS");
        return toDTO(saved);
    }

    @Transactional
    public ScheduleApplicationDTO withdraw(Long id, Long operatorId) {
        ScheduleApplicationEntity entity = getEntity(id);
        requireStatus(entity, "DRAFT", "SUBMITTED");
        String beforeStatus = entity.getStatus();
        entity.setStatus("WITHDRAWN");
        entity.setUpdatedBy(operatorId);
        ScheduleApplicationEntity saved = repository.save(entity);
        operationLogService.createLog("APPLICATION", saved.getId(), "WITHDRAW", operatorId, "TEACHER",
                beforeStatus, saved.getStatus(), "撤回授课申请单", "处理成功");
        noticeService.createNotice("APPLICATION", saved.getId(), operatorId, saved.getTeacherId(),
                "授课申请已撤回", "您的授课申请单已撤回。", "APPLICATION_STATUS");
        return toDTO(saved);
    }

    private ScheduleApplicationEntity getEntity(Long id) {
        ScheduleApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("授课申请单不存在"));
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NotFoundException("授课申请单不存在");
        }
        return entity;
    }

    private void requireStatus(ScheduleApplicationEntity entity, String... statuses) {
        for (String status : statuses) {
            if (status.equals(entity.getStatus())) {
                return;
            }
        }
        throw new IllegalArgumentException("当前状态不允许执行该操作");
    }

    private void copy(ScheduleApplicationDTO dto, ScheduleApplicationEntity entity) {
        entity.setTermId(dto.getTermId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setPreferredBuildingName(dto.getPreferredBuildingName());
        entity.setPreferredRoomNumber(dto.getPreferredRoomNumber());
        entity.setPreferredWeekNo(dto.getPreferredWeekNo());
        entity.setPreferredDayOfWeek(dto.getPreferredDayOfWeek());
        entity.setPreferredDate(dto.getPreferredDate());
        entity.setPreferredTimeSlotId(dto.getPreferredTimeSlotId());
        entity.setApplicationType(dto.getApplicationType());
        entity.setTeachingTaskId(dto.getTeachingTaskId());
        entity.setExpectedLab(dto.getExpectedLab());
        entity.setTargetWeeks(dto.getTargetWeeks());
        entity.setTargetDayOfWeek(dto.getTargetDayOfWeek());
        entity.setTargetTimeSlot(dto.getTargetTimeSlot());
        // 新增人性化字段
        entity.setProjectName(dto.getProjectName());
        entity.setProjectCategory(dto.getProjectCategory());
        entity.setProjectLeader(dto.getProjectLeader());
        entity.setContactPhone(dto.getContactPhone());
        entity.setGrade(dto.getGrade());
        entity.setClassName(dto.getClassName());
        entity.setParticipantCount(dto.getParticipantCount());
        entity.setDuration(dto.getDuration());
        entity.setStudentCount(dto.getStudentCount());
        entity.setGroupCount(dto.getGroupCount());
        entity.setStudentsPerGroup(dto.getStudentsPerGroup());
        entity.setExperimentContent(dto.getExperimentContent());
        entity.setRemark(dto.getRemark());
        entity.setExperimentRequirement(dto.getExperimentRequirement());
        entity.setEquipmentRequirement(dto.getEquipmentRequirement());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    private ScheduleApplicationDTO toDTO(ScheduleApplicationEntity entity) {
        ScheduleApplicationDTO dto = new ScheduleApplicationDTO();
        dto.setId(entity.getId());
        dto.setApplicationNo(entity.getApplicationNo());
        dto.setTermId(entity.getTermId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setPreferredBuildingName(entity.getPreferredBuildingName());
        dto.setPreferredRoomNumber(entity.getPreferredRoomNumber());
        dto.setPreferredWeekNo(entity.getPreferredWeekNo());
        dto.setPreferredDayOfWeek(entity.getPreferredDayOfWeek());
        dto.setPreferredDate(entity.getPreferredDate());
        dto.setPreferredTimeSlotId(entity.getPreferredTimeSlotId());
        dto.setApplicationType(entity.getApplicationType());
        dto.setTeachingTaskId(entity.getTeachingTaskId());
        dto.setExpectedLab(entity.getExpectedLab());
        dto.setTargetWeeks(entity.getTargetWeeks());
        dto.setTargetDayOfWeek(entity.getTargetDayOfWeek());
        dto.setTargetTimeSlot(entity.getTargetTimeSlot());
        // 新增人性化字段
        dto.setProjectName(entity.getProjectName());
        dto.setProjectCategory(entity.getProjectCategory());
        dto.setProjectLeader(entity.getProjectLeader());
        dto.setContactPhone(entity.getContactPhone());
        dto.setGrade(entity.getGrade());
        dto.setClassName(entity.getClassName());
        dto.setParticipantCount(entity.getParticipantCount());
        dto.setDuration(entity.getDuration());
        dto.setStudentCount(entity.getStudentCount());
        dto.setGroupCount(entity.getGroupCount());
        dto.setStudentsPerGroup(entity.getStudentsPerGroup());
        dto.setExperimentContent(entity.getExperimentContent());
        dto.setRemark(entity.getRemark());
        dto.setExperimentRequirement(entity.getExperimentRequirement());
        dto.setEquipmentRequirement(entity.getEquipmentRequirement());
        dto.setReviewComment(entity.getReviewComment());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
