package com.example.labmanage.repository;

import com.example.labmanage.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    List<UserEntity> findByUsernameContainingOrRealNameContaining(String username, String realName);
}
