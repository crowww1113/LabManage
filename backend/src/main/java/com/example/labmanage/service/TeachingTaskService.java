package com.example.labmanage.service;

import com.example.labmanage.dto.ApplicationInfoDTO;
import com.example.labmanage.dto.TeachingTaskDTO;
import com.example.labmanage.entity.*;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingTaskService {
    private final TeachingTaskRepository taskRepository;
    private final ScheduleApplicationRepository applicationRepository;
    private final CourseRepository courseRepository;
    private final TermRepository termRepository;
    private final ClazzRepository clazzRepository;
    private final UserRepository userRepository;

    public List<TeachingTaskDTO> list(Long termId, Long clazzId, Long teacherId) {
        List<TeachingTaskEntity> list;
        if (termId != null && clazzId != null) {
            list = taskRepository.findByTermIdAndClazzId(termId, clazzId);
        } else if (termId != null) {
            list = taskRepository.findByTermId(termId);
        } else {
            list = taskRepository.findAll();
        }

        if (teacherId != null) {
            list = list.stream()
                    .filter(task -> task.getTeacherIds().contains(teacherId))
                    .toList();
        }

        return list.stream().map(this::toDTO).toList();
    }

    public TeachingTaskDTO getById(Long id) {
        return toDTO(taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("教学任务不存在")));
    }

    @Transactional
    public TeachingTaskDTO create(TeachingTaskDTO dto) {
        TeachingTaskEntity entity = new TeachingTaskEntity();
        copy(dto, entity);
        return toDTO(taskRepository.save(entity));
    }

    @Transactional
    public TeachingTaskDTO update(Long id, TeachingTaskDTO dto) {
        TeachingTaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("教学任务不存在"));
        copy(dto, entity);
        return toDTO(taskRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("教学任务不存在");
        }
        taskRepository.deleteById(id);
    }

    private void copy(TeachingTaskDTO dto, TeachingTaskEntity entity) {
        entity.setCourseId(dto.getCourseId());
        entity.setTermId(dto.getTermId());
        entity.setClazzId(dto.getClazzId());
        entity.setTeacherIds(dto.getTeacherIds());
        entity.setStatus(dto.getStatus() == null ? "进行中" : dto.getStatus());
    }

    private TeachingTaskDTO toDTO(TeachingTaskEntity entity) {
        TeachingTaskDTO dto = new TeachingTaskDTO();
        dto.setId(entity.getId());
        dto.setCourseId(entity.getCourseId());
        dto.setTermId(entity.getTermId());
        dto.setClazzId(entity.getClazzId());
        dto.setTeacherIds(entity.getTeacherIds());
        dto.setStatus(entity.getStatus());

        // 填充展示名称
        courseRepository.findById(entity.getCourseId())
                .ifPresent(c -> dto.setCourseName(c.getCnName()));
        termRepository.findById(entity.getTermId())
                .ifPresent(t -> dto.setTermName(t.getTermName()));
        clazzRepository.findById(entity.getClazzId())
                .ifPresent(c -> dto.setClazzName(c.getClazzName()));
        if (!entity.getTeacherIds().isEmpty()) {
            List<UserEntity> teachers = userRepository.findAllById(entity.getTeacherIds());
            dto.setTeacherNames(teachers.stream()
                    .map(UserEntity::getRealName)
                    .collect(Collectors.joining("、")));
        }

        // 查询是否有已通过的授课申请
        Optional<ScheduleApplicationEntity> appOpt = applicationRepository
                .findByTeachingTaskIdAndStatusAndDeletedFalse(entity.getId(), "APPROVED");
        if (appOpt.isPresent()) {
            ScheduleApplicationEntity app = appOpt.get();
            ApplicationInfoDTO appInfo = new ApplicationInfoDTO();
            appInfo.setTargetWeeks(app.getTargetWeeks());
            appInfo.setTargetDayOfWeek(app.getTargetDayOfWeek());
            appInfo.setTargetTimeSlot(app.getTargetTimeSlot());
            appInfo.setExpectedLab(app.getExpectedLab());
            dto.setApplicationInfo(appInfo);
        }

        return dto;
    }
}
