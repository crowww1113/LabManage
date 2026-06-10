package com.example.labmanage.repository;

import com.example.labmanage.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    boolean existsByModuleAndNameAndActionAndIdNot(String module, String name, String action, Long id);

    Optional<PermissionEntity> findByCode(String code);

    List<PermissionEntity> findByModuleContainingOrNameContainingOrActionContaining(String module, String name, String action);
}
