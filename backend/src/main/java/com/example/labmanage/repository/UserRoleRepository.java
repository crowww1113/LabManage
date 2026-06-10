package com.example.labmanage.repository;

import com.example.labmanage.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findByUserId(Long userId);

    @Query("SELECT ur.userId FROM UserRoleEntity ur WHERE ur.roleId IN (SELECT r.id FROM RoleEntity r WHERE r.code IN :roleCodes)")
    List<Long> findUserIdsByRoleCodes(@org.springframework.data.repository.query.Param("roleCodes") List<String> roleCodes);

    @Modifying
    @Query("delete from UserRoleEntity ur where ur.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
