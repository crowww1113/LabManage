package com.example.labmanage.repository;

import com.example.labmanage.entity.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Integer> {
}