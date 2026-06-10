package com.example.labmanage.repository;

import com.example.labmanage.entity.ExpItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpItemRepository extends JpaRepository<ExpItem, Integer> {
}