package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "sys_teaching_task")
public class TeachingTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long termId;

    @Column(nullable = false)
    private Long clazzId;

    @Transient
    private List<Long> teacherIds;

    @Column(name = "teacher_ids", length = 500)
    private String teacherIdsValue;

    @Column(length = 10)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public List<Long> getTeacherIds() {
        if (teacherIds == null) {
            if (teacherIdsValue == null || teacherIdsValue.isBlank()) {
                teacherIds = List.of();
            } else {
                teacherIds = Arrays.stream(teacherIdsValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .toList();
            }
        }
        return teacherIds;
    }

    public void setTeacherIds(List<Long> teacherIds) {
        this.teacherIds = teacherIds == null ? List.of() : List.copyOf(teacherIds);
        this.teacherIdsValue = this.teacherIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @PostLoad
    protected void onLoad() {
        teacherIds = null;
    }

    @PrePersist
    protected void onCreate() {
        syncTeacherIdsValue();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        syncTeacherIdsValue();
        updatedAt = LocalDateTime.now();
    }

    private void syncTeacherIdsValue() {
        if (teacherIds != null) {
            teacherIdsValue = teacherIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }
}
