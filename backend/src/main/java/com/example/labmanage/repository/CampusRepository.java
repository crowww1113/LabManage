package com.example.labmanage.repository;

import com.example.labmanage.entity.CampusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampusRepository extends JpaRepository<CampusEntity, Long> {
    Optional<CampusEntity> findByName(String name);
}
