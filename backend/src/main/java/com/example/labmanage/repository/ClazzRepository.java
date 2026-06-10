package com.example.labmanage.repository;

import com.example.labmanage.entity.ClazzEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClazzRepository extends JpaRepository<ClazzEntity, Long> {
    List<ClazzEntity> findByMajorId(Long majorId);
}