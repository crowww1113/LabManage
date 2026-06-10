package com.example.labmanage.service;

import com.example.labmanage.entity.TeachingTaskEntity;
import com.example.labmanage.repository.ScheduleApplicationRepository;
import com.example.labmanage.repository.TeachingTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeachingTaskServiceTest {

    @Mock
    private TeachingTaskRepository taskRepository;

    @Mock
    private ScheduleApplicationRepository applicationRepository;

    @InjectMocks
    private TeachingTaskService teachingTaskService;

    @Test
    void list_shouldFilterByTermIdWhenOnlyTermIdIsProvided() {
        TeachingTaskEntity task = new TeachingTaskEntity();
        task.setId(1L);
        task.setCourseId(10L);
        task.setTermId(2L);
        task.setClazzId(3L);
        task.setTeacherIds(List.of(4L));
        task.setStatus("进行中");

        when(taskRepository.findByTermId(2L)).thenReturn(List.of(task));
        when(applicationRepository.findByTeachingTaskIdAndStatusAndDeletedFalse(1L, "APPROVED"))
                .thenReturn(Optional.empty());

        var result = teachingTaskService.list(2L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTermId()).isEqualTo(2L);
        verify(taskRepository).findByTermId(2L);
        verify(taskRepository, never()).findAll();
    }
}
