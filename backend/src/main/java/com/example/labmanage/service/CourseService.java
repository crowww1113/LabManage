package com.example.labmanage.service;

import com.example.labmanage.entity.Course;
import com.example.labmanage.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // 课程列表（支持按名称搜索）
    public List<Course> list(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return courseRepository.findAll();
        } else {
            return courseRepository.findByCnNameContaining(keyword);
        }
    }

    // 新增课程
    public void addCourse(Course course) {
        if (course.getCode() == null || course.getCode().isBlank()) {
            long count = courseRepository.count();
            course.setCode("CO" + String.format("%03d", count + 1));
        }
        courseRepository.save(course);
    }

    // 更新课程
    public Course updateCourse(Long id, Course course) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        existing.setCode(course.getCode());
        existing.setCnName(course.getCnName());
        existing.setEnName(course.getEnName());
        existing.setType(course.getType());
        existing.setCredit(course.getCredit());
        existing.setTotalHour(course.getTotalHour());
        existing.setTeachHour(course.getTeachHour());
        existing.setPracticeHour(course.getPracticeHour());
        existing.setLabHour(course.getLabHour());
        existing.setNetHour(course.getNetHour());
        existing.setTerm(course.getTerm());
        return courseRepository.save(existing);
    }

    // 删除课程
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}