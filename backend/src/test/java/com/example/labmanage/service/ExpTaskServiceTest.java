package com.example.labmanage.service;

import com.example.labmanage.entity.ClazzEntity;
import com.example.labmanage.entity.ExpTask;
import com.example.labmanage.entity.TermEntity;
import com.example.labmanage.repository.ClazzRepository;
import com.example.labmanage.repository.ExpTaskRepository;
import com.example.labmanage.repository.TermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpTaskServiceTest {

    @Mock
    private ExpTaskRepository expTaskRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private ClazzRepository clazzRepository;

    @InjectMocks
    private ExpTaskService expTaskService;

    private ExpTask task;

    @BeforeEach
    void setUp() {
        task = new ExpTask();
        task.setId(1);
        task.setTerm("2025-2026-2");
        task.setClassId(10);
    }

    @Test
    void getById_shouldResolveSecondTermIdExactly() {
        TermEntity term = new TermEntity();
        term.setId(2L);
        term.setTermName("2025-2026学年第二学期");
        ClazzEntity clazz = new ClazzEntity();
        clazz.setClazzName("软件工程1班");
        clazz.setGrade("2025");

        when(expTaskRepository.findById(1)).thenReturn(Optional.of(task));
        when(termRepository.findByTermName("2025-2026学年第二学期")).thenReturn(Optional.of(term));
        when(clazzRepository.findById(10L)).thenReturn(Optional.of(clazz));

        ExpTask result = expTaskService.getById(1).orElseThrow();

        assertThat(result.getTermId()).isEqualTo(2L);
        assertThat(result.getClassName()).isEqualTo("软件工程1班");
        assertThat(result.getGrade()).isEqualTo("2025");
    }

    @Test
    void getById_shouldResolveFirstTermIdExactly() {
        task.setTerm("2025-2026-1");
        TermEntity term = new TermEntity();
        term.setId(1L);
        term.setTermName("2025-2026学年第一学期");

        when(expTaskRepository.findById(1)).thenReturn(Optional.of(task));
        when(termRepository.findByTermName("2025-2026学年第一学期")).thenReturn(Optional.of(term));
        when(clazzRepository.findById(10L)).thenReturn(Optional.empty());

        ExpTask result = expTaskService.getById(1).orElseThrow();

        assertThat(result.getTermId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldResolveFullTermNameExactly() {
        task.setTerm("2025-2026学年第二学期");
        TermEntity term = new TermEntity();
        term.setId(2L);
        term.setTermName("2025-2026学年第二学期");

        when(expTaskRepository.findById(1)).thenReturn(Optional.of(task));
        when(termRepository.findByTermName("2025-2026学年第二学期")).thenReturn(Optional.of(term));
        when(clazzRepository.findById(10L)).thenReturn(Optional.empty());

        ExpTask result = expTaskService.getById(1).orElseThrow();

        assertThat(result.getTermId()).isEqualTo(2L);
    }

    @Test
    void getById_shouldLeaveTermIdEmptyWhenTermFormatIsInvalid() {
        task.setTerm("2025秋");

        when(expTaskRepository.findById(1)).thenReturn(Optional.of(task));
        when(clazzRepository.findById(10L)).thenReturn(Optional.empty());

        ExpTask result = expTaskService.getById(1).orElseThrow();

        assertThat(result.getTermId()).isNull();
        verify(termRepository, never()).findByTermName(anyString());
    }

    @Test
    void getById_shouldLeaveTermIdEmptyWhenMappedTermDoesNotExist() {
        when(expTaskRepository.findById(1)).thenReturn(Optional.of(task));
        when(termRepository.findByTermName("2025-2026学年第二学期")).thenReturn(Optional.empty());
        when(clazzRepository.findById(10L)).thenReturn(Optional.empty());

        ExpTask result = expTaskService.getById(1).orElseThrow();

        assertThat(result.getTermId()).isNull();
    }
}
