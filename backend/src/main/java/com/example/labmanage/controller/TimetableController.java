package com.example.labmanage.controller;

import com.example.labmanage.dto.MatrixTimetableDTO;
import com.example.labmanage.dto.TimetableListDTO;
import com.example.labmanage.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * 获取课表矩阵视图
     */
    @GetMapping("/matrix")
    public MatrixTimetableDTO getTimetableMatrix(
            @RequestParam Long termId,
            @RequestParam Integer weekNo,
            @RequestParam String buildingName,
            @RequestParam(required = false) String roomNumber) {
        return timetableService.getTimetableMatrix(termId, weekNo, buildingName, roomNumber);
    }

    /**
     * 获取排课列表视图（分页、排序）
     */
    @GetMapping("/list")
    public Page<TimetableListDTO> getTimetableList(
            @RequestParam Long termId,
            @RequestParam Integer weekNo,
            @RequestParam String buildingName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return timetableService.getTimetableList(termId, weekNo, buildingName, roomNumber, page, size, sort);
    }

    /**
     * 获取某楼宇下的教室/房间号列表
     */
    @GetMapping("/rooms")
    public List<String> getRoomsByBuilding(@RequestParam String buildingName) {
        return timetableService.getRoomsByBuilding(buildingName);
    }
}
