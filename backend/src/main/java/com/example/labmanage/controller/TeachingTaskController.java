package com.example.labmanage.controller;

import com.example.labmanage.dto.TeachingTaskDTO;
import com.example.labmanage.service.TeachingTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teaching-tasks")
@RequiredArgsConstructor
public class TeachingTaskController {
    private final TeachingTaskService taskService;

    @GetMapping
    public List<TeachingTaskDTO> list(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long teacherId
    ) {
        return taskService.list(termId, clazzId, teacherId);
    }

    @GetMapping("/{id}")
    public TeachingTaskDTO get(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN','TEACHER')")
    public TeachingTaskDTO create(@Valid @RequestBody TeachingTaskDTO dto) {
        return taskService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN','TEACHER')")
    public TeachingTaskDTO update(@PathVariable Long id, @Valid @RequestBody TeachingTaskDTO dto) {
        return taskService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}