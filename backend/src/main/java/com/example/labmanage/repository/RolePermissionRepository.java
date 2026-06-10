package com.example.labmanage.repository;

import com.example.labmanage.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {
    List<RolePermissionEntity> findByRoleId(Long roleId);

    @Modifying
    @Query("delete from RolePermissionEntity rp where rp.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
