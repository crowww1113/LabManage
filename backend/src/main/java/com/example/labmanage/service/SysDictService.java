package com.example.labmanage.service;

import com.example.labmanage.entity.SysDictEntity;
import com.example.labmanage.repository.SysDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictRepository sysDictRepository;

    /**
     * 获取所有字典，按 dictType 分组（有序）
     */
    public Map<String, List<SysDictEntity>> getAllGrouped() {
        List<SysDictEntity> all = sysDictRepository.findAll();
        return all.stream()
                .filter(d -> "启用".equals(d.getStatus()))
                .collect(Collectors.groupingBy(
                        SysDictEntity::getDictType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * 按类型获取字典列表
     */
    public List<SysDictEntity> getByType(String dictType) {
        return sysDictRepository.findByDictTypeOrderBySortOrderAsc(dictType).stream()
                .filter(d -> "启用".equals(d.getStatus()))
                .toList();
    }
}
