package com.example.labmanage.repository;

import com.example.labmanage.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<TermEntity, Long> {
    Optional<TermEntity> findByTermName(String termName);
}
