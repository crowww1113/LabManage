package com.example.labmanage.repository;

import com.example.labmanage.entity.MajorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MajorRepository extends JpaRepository<MajorEntity, Long> {
    List<MajorEntity> findByDeptId(Long deptId);
}