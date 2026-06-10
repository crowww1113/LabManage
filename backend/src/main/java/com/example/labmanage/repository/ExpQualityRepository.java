package com.example.labmanage.repository;

import com.example.labmanage.entity.ExpQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpQualityRepository extends JpaRepository<ExpQuality, Integer> {
}
