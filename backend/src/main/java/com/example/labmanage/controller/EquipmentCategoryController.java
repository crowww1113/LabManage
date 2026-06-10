package com.example.labmanage.controller;

import com.example.labmanage.dto.EquipmentCategoryCreateRequest;
import com.example.labmanage.dto.EquipmentCategoryDTO;
import com.example.labmanage.service.EquipmentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment-categories")
@RequiredArgsConstructor
public class EquipmentCategoryController {

    private final EquipmentCategoryService categoryService;

    @GetMapping
    public List<EquipmentCategoryDTO> list() {
        return categoryService.list();
    }

    @GetMapping("/{id}")
    public EquipmentCategoryDTO get(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentCategoryDTO create(@Valid @RequestBody EquipmentCategoryCreateRequest req) {
        return categoryService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentCategoryDTO update(@PathVariable Long id, @Valid @RequestBody EquipmentCategoryCreateRequest req) {
        return categoryService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Map.of("code", 200, "message", "删除成功");
    }
}
