package com.example.labmanage.repository;

import com.example.labmanage.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
