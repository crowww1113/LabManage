package com.example.labmanage.repository;

import com.example.labmanage.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // 按课程名称模糊查询
    List<Course> findByCnNameContaining(String keyword);
}