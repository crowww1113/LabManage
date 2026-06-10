package com.example.labmanage.repository;

import com.example.labmanage.entity.ExpTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpTaskRepository extends JpaRepository<ExpTask, Integer> {
}