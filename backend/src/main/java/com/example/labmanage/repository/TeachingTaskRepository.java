package com.example.labmanage.repository;

import com.example.labmanage.entity.TeachingTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingTaskRepository extends JpaRepository<TeachingTaskEntity, Long> {
    List<TeachingTaskEntity> findByTermIdAndClazzId(Long termId, Long clazzId);

    List<TeachingTaskEntity> findByTermId(Long termId);
}
