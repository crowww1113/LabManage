package com.example.labmanage.controller;

import com.example.labmanage.entity.Course;
import com.example.labmanage.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teach")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 课程列表接口
    @GetMapping("/courses")
    public List<Course> getCourseList(@RequestParam(required = false) String keyword) {
        return courseService.list(keyword);
    }

    // 新增课程接口
    @PostMapping("/courses")
    public void addCourse(@RequestBody Course course) {
        courseService.addCourse(course);
    }

    // 更新课程
    @PutMapping("/courses/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    // 删除课程
    @DeleteMapping("/courses/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}