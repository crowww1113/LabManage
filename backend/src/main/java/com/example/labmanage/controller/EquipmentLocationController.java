package com.example.labmanage.controller;

import com.example.labmanage.dto.EquipmentLocationCreateRequest;
import com.example.labmanage.dto.EquipmentLocationDTO;
import com.example.labmanage.service.EquipmentLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment-locations")
@RequiredArgsConstructor
public class EquipmentLocationController {

    private final EquipmentLocationService locationService;

    @GetMapping
    public List<EquipmentLocationDTO> list() {
        return locationService.list();
    }

    @GetMapping("/{id}")
    public EquipmentLocationDTO get(@PathVariable Long id) {
        return locationService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentLocationDTO create(@Valid @RequestBody EquipmentLocationCreateRequest req) {
        return locationService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentLocationDTO update(@PathVariable Long id, @Valid @RequestBody EquipmentLocationCreateRequest req) {
        return locationService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> delete(@PathVariable Long id) {
        locationService.delete(id);
        return Map.of("code", 200, "message", "删除成功");
    }
}
