package com.example.labmanage.repository;

import com.example.labmanage.entity.OrgEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgRepository extends JpaRepository<OrgEntity, Long> {
    Optional<OrgEntity> findByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    List<OrgEntity> findByParentId(Long parentId);
    List<OrgEntity> findByStatus(String status);
}
