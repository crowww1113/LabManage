package com.example.labmanage.controller;

import com.example.labmanage.entity.SysDictEntity;
import com.example.labmanage.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    /**
     * 获取所有字典，按 dictType 分组
     */
    @GetMapping("/all")
    public Map<String, List<SysDictEntity>> getAll() {
        return sysDictService.getAllGrouped();
    }

    /**
     * 按类型获取字典列表
     */
    @GetMapping("/{dictType}")
    public List<SysDictEntity> getByType(@PathVariable String dictType) {
        return sysDictService.getByType(dictType);
    }
}
